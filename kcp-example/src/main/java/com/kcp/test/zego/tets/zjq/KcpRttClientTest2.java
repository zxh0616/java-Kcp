package com.kcp.test.zego.tets.zjq;

import cn.hutool.json.JSONUtil;
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
import java.util.HashMap;
import java.util.Map;
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
public class KcpRttClientTest2 implements KcpListener {

    static final Logger log = LoggerFactory.getLogger(KcpRttClientTest2.class);
    private final ByteBuf data;
    private final long startTime;
    private int[] rtts;
    private volatile int count;
    private ScheduledExecutorService scheduleSrv;
    private ScheduledFuture<?> future = null;

    public KcpRttClientTest2() {
        data = Unpooled.buffer(200);
        for (int i = 0; i < data.capacity(); i++) {
            data.writeByte((byte) i);
        }

        rtts = new int[300];
        for (int i = 0; i < rtts.length; i++) {
            rtts[i] = -1;
        }
        startTime = System.currentTimeMillis();
        scheduleSrv = new ScheduledThreadPoolExecutor(1);
    }

    public static void main(String[] args) {
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 2, true);
        channelConfig.setSndwnd(512);
        channelConfig.setRcvwnd(512);
        channelConfig.setMtu(100);
        channelConfig.setAckNoDelay(true);
        channelConfig.setAckMaskSize(8);
        //channelConfig.setStream(true);
        //channelConfig.setConv(111);
        //channelConfig.setFecAdapt(new FecAdapt(3,1));
        //channelConfig.setCrc32Check(true);
        channelConfig.setTimeoutMillis(100000);
        //channelConfig.setAckMaskSize(32);
        KcpClient kcpClient = new KcpClient();
        kcpClient.init(channelConfig);

        KcpRttClientTest2 kcpClientRttExample = new KcpRttClientTest2();
        kcpClient.connect(new InetSocketAddress("127.0.0.1", 20003), channelConfig, kcpClientRttExample);
        //kcpClient.connect(new InetSocketAddress("10.60.100.191",20003),channelConfig,kcpClientRttExample);
    }

    @Override
    public void onConnected(Ukcp ukcp) {
        log.info("连接成功-onConnected, 会话ID= {}, 客户端IP={}, 服务端IP= {}", ukcp.getConv(), ukcp.user().getLocalAddress(), ukcp.user().getRemoteAddress());
        // ■ 登录房间【IKCP_CMD_LOGIN_ROOM】 1
        ByteBuf byteBuf = Unpooled.buffer(10);
        byteBuf.writeShort(1);

        HashMap<String, String> map = new HashMap<>();
        map.put("roomId", "123");
        map.put("userId", "789");
        byteBuf.writeBytes(JSONUtil.toJsonStr(map).getBytes());
        ukcp.write(byteBuf);
        byteBuf.release();
    }


    /*
     * ■ 登录房间【IKCP_CMD_LOGIN_ROOM] 1
     * ■ 登录房间成功【IKCP_CMD_LOGIN_ROOM_success] 11
     * ■ 登出房间【IKCP_CMD_LOGOUT_ROOM] 2
     * ■ 登出房间成功【IKCP_CMD_LOGOUT_ROOM_success] 22
     *
     * */
    @Override
    public void handleReceive(ByteBuf buf, Ukcp ukcp) {
        int flag = buf.readShort();
        if (flag == 11) {
            log.info("开始拉流推流");

            ByteBuf buffer = Unpooled.buffer(10);
            buffer.writeShort(2);
            HashMap<String, String> map = new HashMap<>();
            map.put("roomId", "123");
            map.put("userId", "456");
            //map.put("ts", String.valueOf(System.currentTimeMillis() -startTime));
            map.put("data", "客户端B");
            buffer.writeBytes(JSONUtil.toJsonStr(map).getBytes());
            ukcp.write(buffer);
            buffer.release();

        } else if (flag == 22) {
            int readableBytes = buf.readableBytes();
            byte[] contentBytes = new byte[readableBytes];
            buf.readBytes(contentBytes);
            Map bean = JSONUtil.toBean(new String(contentBytes), Map.class);
            String data = (String) bean.get("data");
            //long ts = Long.parseLong((String) bean.get("ts"));
            //log.info("收到数据={}, 时间ts= {}", data, System.currentTimeMillis() -startTime -ts);
            log.info("收到数据={}", data);
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
    public ByteBuf rttMsg(int count) {
        ByteBuf buf = Unpooled.buffer(10);
        buf.writeShort(count);
        buf.writeInt((int) (System.currentTimeMillis() - startTime));

        //int dataLen = new Random().nextInt(200);
        //buf.writeBytes(new byte[dataLen]);

        int dataLen = data.readableBytes();
        buf.writeShort(dataLen);
        buf.writeBytes(data, data.readerIndex(), dataLen);

        return buf;
    }

}
