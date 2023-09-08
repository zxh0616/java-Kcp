package com.kcp.test.zego;


import lombok.Data;

@Data
public class ZegoRoomConfig {

    //详情描述: 房间最大用户数量，传 0 视为不限制，默认无限制
    public int maxMemberCount;

    //详情描述: 是否开启用户进出房间回调通知 [onRoomUserUpdate]，默认关闭。若开发者需要使用 ZEGO 房间用户广播通知，请确保每个登录的用户都将此标记设置为true
    public boolean isUserStatusNotify;

    public String token;
}
