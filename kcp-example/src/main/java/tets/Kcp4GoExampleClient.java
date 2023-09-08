package tets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kcp.KcpListener;
import kcp.Ukcp;

import java.nio.ByteOrder;

/**
 * 与go版本兼容的客户端
 * Created by JinMiao
 * 2019/11/29.
 */
public class Kcp4GoExampleClient implements KcpListener {

    public static void main(String[] args) {
//        ChannelConfig channelConfig = new ChannelConfig();
//        channelConfig.nodelay(true,40,2,true);
//        channelConfig.setSndwnd(1024);
//        channelConfig.setRcvwnd(1024);
//        channelConfig.setMtu(1400);
//        channelConfig.setFecAdapt(new FecAdapt(10,3));
//        channelConfig.setAckNoDelay(false);
//        //channelConfig.setTimeoutMillis(10000);
//
//        //禁用参数
//        channelConfig.setCrc32Check(false);
//        channelConfig.setAckMaskSize(0);
//
//
//        KcpClient kcpClient = new KcpClient();
//        kcpClient.init(channelConfig);
//
//
//        Kcp4GoExampleClient kcpGoExampleClient = new Kcp4GoExampleClient();
//        Ukcp ukcp = kcpClient.connect(new InetSocketAddress("127.0.0.1", 10000), channelConfig, kcpGoExampleClient);
//        String msg = "hello!!!!!11111111111111111111111111";
//        byte[] bytes = msg.getBytes();
//        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer(bytes.length);
//        byteBuf.writeBytes(bytes);
//        ukcp.write(byteBuf);

        ByteBuf buffer = Unpooled.buffer(10).order(ByteOrder.BIG_ENDIAN);

        buffer.writeByte(127);         // Write a byte
        buffer.writeShort(32767);      // Write a short (16-bit)
        buffer.writeInt(2147483647);   // Write an int (32-bit)

        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);

        for (byte b : bytes) {
            System.out.print(b + " ");
        }

    }

    @Override
    public void onConnected(Ukcp ukcp) {

    }

    @Override
    public void handleReceive(ByteBuf byteBuf, Ukcp ukcp) {

    }

    @Override
    public void handleException(Throwable ex, Ukcp ukcp) {

    }

    @Override
    public void handleClose(Ukcp ukcp) {

    }
}
