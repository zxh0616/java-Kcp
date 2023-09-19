package tets.zjq;

import com.backblaze.erasure.fec.Snmp;
import io.netty.buffer.ByteBuf;
import kcp.ChannelConfig;
import kcp.KcpListener;
import kcp.KcpServer;
import kcp.Ukcp;

/**
 * 测试延迟的例子
 * Created by JinMiao
 * 2018/11/2.
 */
public class KcpRttServer implements KcpListener {

    public static void main(String[] args) {

        KcpRttServer kcpRttExampleServer = new KcpRttServer();

        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 2, true);
        channelConfig.setSndwnd(512);
        channelConfig.setRcvwnd(512);
        channelConfig.setMtu(100);
        channelConfig.setAckMaskSize(8);
        //channelConfig.setStream(true);
        //channelConfig.setFecAdapt(new FecAdapt(3,1));
        channelConfig.setAckNoDelay(true);
        channelConfig.setTimeoutMillis(100000);
        channelConfig.setUseConvChannel(true);
        //channelConfig.setCrc32Check(true);
        KcpServer kcpServer = new KcpServer();
        kcpServer.init(kcpRttExampleServer, channelConfig, 20003);
    }


    @Override
    public void onConnected(Ukcp ukcp) {
        System.out.println("有连接进来" + Thread.currentThread().getName() + ukcp.user().getRemoteAddress());
    }

    @Override
    public void handleReceive(ByteBuf buf, Ukcp kcp) {
        //ByteBuf copyBuf = buf.copy();

        short curCount = buf.getShort(buf.readerIndex());
        System.out.println(Thread.currentThread().getName() + "  收到消息 " + curCount);
        kcp.write(buf);
        if (curCount == -1) {
            kcp.close();
        }
    }

    @Override
    public void handleException(Throwable ex, Ukcp kcp) {
        ex.printStackTrace();
    }

    @Override
    public void handleClose(Ukcp kcp) {
        System.out.println(Snmp.snmp.toString());
        Snmp.snmp = new Snmp();
    }
}
