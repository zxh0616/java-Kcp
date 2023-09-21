package tets;

import cn.hutool.core.util.StrUtil;
import com.backblaze.erasure.fec.Snmp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kcp.ChannelConfig;
import kcp.KcpClient;
import kcp.KcpListener;
import kcp.Ukcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 测试延迟的例子
 * Created by JinMiao
 * 2019-06-26.
 */

@Component
public class KcpRttExampleClient implements KcpListener {

    //public static Ukcp ukcp;
    //public static String  roomId;
    static final Logger logger = LoggerFactory.getLogger(KcpRttExampleClient.class);
    private final ByteBuf data;
    private final long startTime;
    private int[] rtts;
    private volatile int count;
    private ScheduledExecutorService scheduleSrv;
    private ScheduledFuture<?> future = null;

    public KcpRttExampleClient() {
        data = Unpooled.buffer(250);
        for (int i = 0; i < data.capacity(); i++) {
            data.writeByte((byte) i);
            //data.writeBytes("456456".getBytes());
        }
        rtts = new int[300];
        for (int i = 0; i < rtts.length; i++) {
            rtts[i] = -2;
        }
        startTime = System.currentTimeMillis();
        scheduleSrv = new ScheduledThreadPoolExecutor(1);
    }

    public static void main(String[] args) {

        ChannelConfig channelConfig = new ChannelConfig();
        //channelConfig.nodelay(true, 400000000, 2, true);
        channelConfig.nodelay(true, 40, 2, true);
        //channelConfig.nodelay(true,40,4,false);
        channelConfig.setSndwnd(512);
        channelConfig.setRcvwnd(512);
        channelConfig.setMtu(300);
        channelConfig.setAckMaskSize(8);
        channelConfig.setAckNoDelay(true);
        //channelConfig.setStream(true);
        //channelConfig.setConv(1111);
        channelConfig.setConv(2222);

        //channelConfig.setFecAdapt(new FecAdapt(3,1));
        //channelConfig.setCrc32Check(true);
        //channelConfig.setTimeoutMillis(10000);
        //channelConfig.setAckMaskSize(32);
        KcpClient kcpClient = new KcpClient();
        kcpClient.init(channelConfig);

        KcpRttExampleClient kcpClientRttExample = new KcpRttExampleClient();
        //kcpClient.connect(new InetSocketAddress("47.102.147.143", 20003), channelConfig, kcpClientRttExample);
        kcpClient.connect(new InetSocketAddress("127.0.0.1", 20003), channelConfig, kcpClientRttExample);

        //kcpClient.connect(new InetSocketAddress("10.60.100.191",20003),channelConfig,kcpClientRttExample);
    }

    @Override
    public void onConnected(Ukcp ukcp) {


        //Map<String, String> map = new HashMap<>();
        //map.put("eventName", "loginRoom");
        //map.put("roomId", "9999");
        //map.put("userId", "1111");
        //String jsonStr = JSONUtil.toJsonStr(map);
        ByteBuf buf = Unpooled.buffer(10);
        buf.writeInt(0);
        buf.writeBytes("9999".getBytes());
        ukcp.write(buf);
        buf.release();
    }

    /**
     * 0 loginRoom
     * 1 loginRoomSuccess
     * 2 startStream
     * 3  推流
     *
     * @param buf  the data
     * @param ukcp
     */
    @Override
    public void handleReceive(ByteBuf buf, Ukcp ukcp) {

        int flag = buf.readInt();
        if (flag == 1) {
            int readableBytes = buf.readableBytes();
            byte[] contentBytes = new byte[readableBytes];
            buf.readBytes(contentBytes);
            String content = new String(contentBytes);
            if (StrUtil.isBlank(content)) {
                logger.error("接收信息为空");
            }
            String[] split = content.split("_");
            String roomId = split[0];
            String userId = split[1];
            logger.info("登录房间成功, roomId:{}, userId: {}", roomId, userId);
            return;
        }


        if (flag == 2) {
            int readableBytes = buf.readableBytes();
            byte[] contentBytes = new byte[readableBytes];
            buf.readBytes(contentBytes);
            String content = new String(contentBytes);
            if (StrUtil.isBlank(content)) {
                logger.error("接收信息为空");
            }
            String roomId = content;
            logger.info("开始推流，房间Id：{}", roomId);

            //ByteBuf byteBuf = rttMsg(3, roomId);
            //ukcp.write(byteBuf);
            //byteBuf.release();
            future = scheduleSrv.scheduleWithFixedDelay(() -> {
                if (count++ >= rtts.length) {
                    // finish
                    future.cancel(true);
                    ByteBuf byteBuf = rttMsg(-1, roomId);
                    ukcp.write(byteBuf);
                    byteBuf.release();
                } else {
                    ByteBuf byteBuf = rttMsg(count, roomId);
                    ukcp.write(byteBuf);
                    byteBuf.release();
                }
            }, 1, 1, TimeUnit.MILLISECONDS);
            return;
        }
        if (flag == 4) {
            int curCount = buf.readShort();
            logger.info("开始拉流，房间Id：9999, curCount:{}", curCount);
            //int idx = curCount - 1;
            //long time = buf.readInt();
            //logger.info("rcv count {} {}", curCount, System.currentTimeMillis());
            //rtts[idx] = (int) (System.currentTimeMillis() - startTime - time);
            //System.out.println("rtt : "+ curCount+"  "+ rtts[idx]);

            if (curCount == -1) {
                scheduleSrv.schedule(() -> {
                    int sum = 0;
                    for (int rtt : rtts) {
                        sum += rtt;
                    }
                    logger.info("average: " + (sum / rtts.length));
                    logger.info("客户端Snmp:" + Snmp.snmp.toString());
                    ukcp.close();
                    //ukcp.setTimeoutMillis(System.currentTimeMillis());
                    System.exit(0);
                }, 3, TimeUnit.SECONDS);
            } else {
                int idx = curCount - 1;
                long time = buf.readInt();
                if (rtts[idx] != -1) {
                    System.out.println("???");
                }
                logger.info("rcv count {} {}", curCount, System.currentTimeMillis());
                //rtts[idx] = (int) (System.currentTimeMillis() - startTime - time);
                rtts[idx] = (int) (System.currentTimeMillis() - time);
                System.out.println("rtt : " + curCount + "  " + rtts[idx]);
            }
        }

    }

    @Override
    public void handleException(Throwable ex, Ukcp kcp) {
        ex.printStackTrace();
    }

    @Override
    public void handleClose(Ukcp kcp) {
        scheduleSrv.shutdown();
        try {
            scheduleSrv.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("结束了");
        int sum = 0;
        int max = 0;
        for (int rtt : rtts) {
            if (rtt > max) {
                max = rtt;
            }
            sum += rtt;
        }
        System.out.println("average: " + (sum / rtts.length) + " max:" + max);
        System.out.println(Snmp.snmp.toString());
        System.out.println("lost percent: " + (Snmp.snmp.RetransSegs.doubleValue() / Snmp.snmp.OutPkts.doubleValue()));


    }


    /**
     * count+timestamp+dataLen+data
     *
     * @param count
     * @return
     */
    public ByteBuf rttMsg(int count, String roomId) {
        logger.info("send count {} {}", count, System.currentTimeMillis());
        ByteBuf buf = Unpooled.buffer(10);
        buf.writeInt(3);
        buf.writeInt(4);
        //buf.writeBytes(roomId.getBytes(), 0, 4);
        buf.writeShort(count);

        buf.writeInt((int) (System.currentTimeMillis()));
        //int dataLen = new Random().nextInt(200);
        //buf.writeBytes(new byte[dataLen]);

        int dataLen = data.readableBytes();
        buf.writeShort(dataLen);
        buf.writeBytes(data, data.readerIndex(), dataLen);

        return buf;
    }

}
