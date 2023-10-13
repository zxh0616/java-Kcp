package kcp;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * Created by JinMiao
 * 2018/11/2.
 */
public class User {

    private Channel channel;
    private InetSocketAddress remoteAddress;
    private InetSocketAddress localAddress;

    /*-----------------2023.10.10  zjq--------------------*/
    //"identity"" "对方身份：0：房主，1：加入者",
    private String identity = "1";

    private Boolean publishingFlag;
    private String publishingStreamId;

    private Boolean playingFlag;
    private String playingStreamId;

    private Object cache;

    public void setCache(Object cache) {
        this.cache = cache;
    }

    public <T>  T getCache() {
        return (T) cache;
    }

    public User(Channel channel, InetSocketAddress remoteAddress, InetSocketAddress localAddress) {
        this.channel = channel;
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
    }

    protected Channel getChannel() {
        return channel;
    }

    protected void setChannel(Channel channel) {
        this.channel = channel;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    protected void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    protected void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Boolean getPublishingFlag() {
        return publishingFlag;
    }

    public void setPublishingFlag(Boolean publishingFlag) {
        this.publishingFlag = publishingFlag;
    }

    public String getPublishingStreamId() {
        return publishingStreamId;
    }

    public void setPublishingStreamId(String publishingStreamId) {
        this.publishingStreamId = publishingStreamId;
    }

    public Boolean getPlayingFlag() {
        return playingFlag;
    }

    public void setPlayingFlag(Boolean playingFlag) {
        this.playingFlag = playingFlag;
    }

    public String getPlayingStreamId() {
        return playingStreamId;
    }

    public void setPlayingStreamId(String playingStreamId) {
        this.playingStreamId = playingStreamId;
    }

    @Override
    public String toString() {
        return "User{" +
                "remoteAddress=" + remoteAddress +
                ", localAddress=" + localAddress +
                '}';
    }
}
