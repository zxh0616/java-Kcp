package kcp;

import com.backblaze.erasure.fec.Snmp;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import threadPool.ITask;

import java.io.IOException;
import java.util.Queue;

/**
 * Created by JinMiao
 * 2018/9/11.
 */
public class WriteTask implements ITask {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(WriteTask.class);

    @Override
    public String toString() {
        return "WriteTask{" +
                "ukcp=" + ukcp +
                '}';
    }

    private final Ukcp ukcp;

    public WriteTask(Ukcp ukcp) {
        this.ukcp = ukcp;
    }

    @Override
    public void execute() {
        Ukcp ukcp = this.ukcp;
        try {
            //查看连接状态
            if(!ukcp.isActive()){
                return;
            }
            //从KCP写缓冲区 到  发送缓冲区
            Queue<ByteBuf> queue = ukcp.getWriteBuffer();

            int writeCount =0;
            long writeBytes = 0;
            while(ukcp.canSend(false)){
                ByteBuf byteBuf = queue.poll();
                if(byteBuf==null){
                    break;
                }
                writeCount++;
                try {
                    writeBytes +=byteBuf.readableBytes();
                    if (log.isDebugEnabled()) {
                        log.debug("{} WriteTask: byteBuf.readableBytes()={}", this, byteBuf.readableBytes());
                    }
                    ukcp.send(byteBuf);
                    byteBuf.release();
                } catch (IOException e) {
                    ukcp.getKcpListener().handleException(e, ukcp);
                    return;
                }
            }
            Snmp.snmp.BytesSent.add(writeBytes); // 记录从上级发送的字节
            //是否控制 写入缓冲区大小
            if(ukcp.isControlWriteBufferSize()){
                ukcp.getWriteBufferIncr().addAndGet(writeCount);
            }
            //如果有发送 则检测时间
            if(!ukcp.canSend(false)||(ukcp.checkFlush()&& ukcp.isFastFlush())){

                long now =System.currentTimeMillis();
                long next = ukcp.flush(now);
                ukcp.setTsUpdate(now+next);
            }
        }catch (Throwable e){
            e.printStackTrace();
        }finally {
            release();
        }
    }


    public void release(){
        ukcp.getWriteProcessing().set(false);
    }
}
