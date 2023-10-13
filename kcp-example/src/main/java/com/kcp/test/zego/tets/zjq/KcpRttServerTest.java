package com.kcp.test.zego.tets.zjq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.backblaze.erasure.fec.Snmp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kcp.ChannelConfig;
import kcp.KcpListener;
import kcp.KcpServer;
import kcp.Ukcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 测试延迟的例子
 * Created by JinMiao
 * 2018/11/2.
 */
public class KcpRttServerTest implements KcpListener {

    static final Logger log = LoggerFactory.getLogger(KcpRttServerTest.class);
    public Map<String, Map<String, Ukcp>> rooms = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        KcpRttServerTest kcpRttExampleServer = new KcpRttServerTest();

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
        //channelConfig.setUseConvChannel(true);
        //channelConfig.setCrc32Check(true);
        KcpServer kcpServer = new KcpServer();
        kcpServer.init(kcpRttExampleServer, channelConfig, 20003);
    }

    @Override
    public void onConnected(Ukcp ukcp) {
        log.info("新连接，线程名= {}, 会话ID= {}, 客户端IP= {}", Thread.currentThread().getName(), ukcp.getConv(), ukcp.user().getRemoteAddress());
    }

    @Override
    public void handleReceive(ByteBuf buf, Ukcp kcp) {
        int flag = buf.readShort();
        //log.info("收到消息，线程名= {}", Thread.currentThread().getName());
        /*
         * ■ 登录房间【IKCP_CMD_LOGIN_ROOM] 1
         * ■ 登录房间成功【IKCP_CMD_LOGIN_ROOM_success] 11
         * ■ 登出房间【IKCP_CMD_LOGOUT_ROOM] 2
         * ■ 登出房间成功【IKCP_CMD_LOGOUT_ROOM_success] 22
         *
         * ■ 开始拉流【IKCP_CMD_START_PLAYING_STREAM】 3
         * ■ 开始推流【IKCP_CMD_START_PUBLISHING_STREAM】 4
         * ■ 停止拉流【IKCP_CMD_STOP_PLAYING_STREAM】 5
         * ■ 停止推流【IKCP_CMD_STOP_PUBLISHING_STREAM】 6
         *
         * */

        int readableBytes = buf.readableBytes();
        byte[] contentBytes = new byte[readableBytes];
        buf.readBytes(contentBytes);
        Map bean = JSONUtil.toBean(new String(contentBytes), Map.class);
        if (flag == 1) {
            String roomId = (String) bean.get("roomId");
            String userId = (String) bean.get("userId");

            //TODO 判断 roomId、userId 非空
            Map<String, Ukcp> room = rooms.get(roomId);
            if (CollUtil.isEmpty(room)) {
                room = new HashMap<>();
                //房主
                kcp.user().setIdentity("0");
                room.put(userId, kcp);
                rooms.put(roomId, room);

            } else {
                room.put(userId, kcp);
            }

            log.info("回复登录房间成功");
            if (room.size() == 2) {
                //房间已满，通知用户去开始K歌
                ByteBuf byteBuf = Unpooled.buffer(10);
                byteBuf.writeShort(11);
                byteBuf.writeBytes(contentBytes);
                kcp.write(byteBuf);
                Ukcp ukcp = room.get(room.keySet().stream().filter(t -> !t.equals(userId)).findFirst().orElse(null));
                ukcp.write(byteBuf);
                byteBuf.release();
            }
        } else if (flag == 2) {
            log.info("转发数据");

            String roomId = (String) bean.get("roomId");
            String sendUserId = (String) bean.get("userId");

            Map<String, Ukcp> room = rooms.get(roomId);
            Ukcp sendUkcp = room.get(room.keySet().stream().filter(t -> t.equals(sendUserId)).findFirst().orElse(null));

            ByteBuf byteBuf = Unpooled.buffer(10);
            byteBuf.writeShort(22);
            byteBuf.writeBytes(contentBytes);
            sendUkcp.write(byteBuf);
            byteBuf.release();
        }
        //kcp.close();
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
