package com.kcp.test.zego.tets.zjq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.backblaze.erasure.fec.Snmp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kcp.*;
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
public class KcpRttServer implements KcpListener {

    static final Logger log = LoggerFactory.getLogger(KcpRttServer.class);
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
    public Map<String, Map<String, Ukcp>> rooms = new ConcurrentHashMap();

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
        if (flag == 1) {
            int readableBytes = buf.readableBytes();
            byte[] contentBytes = new byte[readableBytes];
            buf.readBytes(contentBytes);
            Map bean = JSONUtil.toBean(new String(contentBytes), Map.class);
            String roomId = (String) bean.get("roomId");
            String userId = (String) bean.get("userId");

            //TODO 判断 roomId、userId 非空
            Map<String, Ukcp> room = rooms.get(roomId);
            if (CollUtil.isEmpty(room)) {
                room = new HashMap<>();
                //房主
                kcp.user().setIdentity("0");
            }
            room.put(userId, kcp);
            log.info("回复登录房间成功");
            ByteBuf byteBuf = Unpooled.buffer(10);
            byteBuf.writeShort(11);
            byteBuf.writeBytes(contentBytes);
            kcp.write(byteBuf);
            byteBuf.release();
            return;

        } else if (flag == 2) {
            log.info("回复登出房间成功");
            ByteBuf byteBuf = Unpooled.buffer(10);
            byteBuf.writeShort(22);
            kcp.write(byteBuf);
            byteBuf.release();
            return;

        } else if (flag == 3) {
            log.info("回复拉流开启成功");
            int readableBytes = buf.readableBytes();
            byte[] contentBytes = new byte[readableBytes];
            buf.readBytes(contentBytes);
            String playingStreamId = new String(contentBytes);
            //更新用户信息
            User user = kcp.user();
            user.setPlayingFlag(true);
            user.setPlayingStreamId(playingStreamId);

            ByteBuf byteBuf = Unpooled.buffer(10);
            byteBuf.writeShort(33);
            kcp.write(byteBuf);
            byteBuf.release();
            return;

        } else if (flag == 4) {
            log.info("回复推流开启成功");
            ByteBuf byteBuf = Unpooled.buffer(10);
            byteBuf.writeShort(44);
            kcp.write(byteBuf);
            byteBuf.release();
            return;

        } else if (flag == 5) {
            log.info("回复拉流停止成功");
            ByteBuf byteBuf = Unpooled.buffer(10);
            byteBuf.writeShort(55);
            kcp.write(byteBuf);
            byteBuf.release();
            return;

        } else if (flag == 6) {
            log.info("回复推流停止成功");
            ByteBuf byteBuf = Unpooled.buffer(10);
            byteBuf.writeShort(66);
            kcp.write(byteBuf);
            byteBuf.release();
            return;
        }


        if (flag == 1) {
            int readableBytes = buf.readableBytes();
            byte[] contentBytes = new byte[readableBytes];
            buf.readBytes(contentBytes);
            String content = new String(contentBytes);
            //if(StrUtil.isBlank(content)){
            //    logger.error("接收信息为空");
            //}
            String roomId = content;
            //String userId = bean.get("userId");

            Map<String, Ukcp> ukcpMap = rooms.get(roomId);
            if (CollUtil.isEmpty(ukcpMap)) {
                ukcpMap = new HashMap<>();
                ukcpMap.put("" + kcp.getConv(), kcp);
                rooms.put(roomId, ukcpMap);
            }
            ukcpMap.put("" + kcp.getConv(), kcp);
            rooms.put(roomId, ukcpMap);
            ByteBuf buffer = Unpooled.buffer(10);
            if (ukcpMap.size() == 2) {
                String userId = "1111";
                //推流
                Ukcp publishingStreamUkcp = ukcpMap.get(userId);
                buffer.writeInt(2);
                buffer.writeBytes("9999".getBytes());
                publishingStreamUkcp.write(buffer);
                log.info("通知客户端【111】开始推流");
                return;
            }

            buffer.writeInt(1);
            buffer.writeBytes(("9999_" + kcp.getConv()).getBytes());
            kcp.write(buffer);
            log.info("加入房间成功，房间ID：{}", roomId);
            return;
        }

        if (flag == 3) {
            //byte[] roomIdBytes = new byte[4];
            //buf.readBytes(roomIdBytes);
            //String roomId = new String(roomIdBytes);
            String roomId = "9999";

            //int readableBytes = buf.readableBytes();
            //byte[] contentBytes = new byte[readableBytes];
            //buf.readBytes(contentBytes);
            //String content = new String(contentBytes);
            //logger.info("接收到的信息：{}" , content);

            Ukcp playingStreamUkcp = rooms.get(roomId).get("2222");
            playingStreamUkcp.write(buf);
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
