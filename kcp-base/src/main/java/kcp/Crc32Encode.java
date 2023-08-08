package kcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * Created by JinMiao
 * 2019/12/10.
 */
public class Crc32Encode extends ChannelOutboundHandlerAdapter {

    private CRC32 crc32 = new CRC32();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        /*
         1、获取 msg 参数，该参数应该是一个 DatagramPacket 对象，表示要发送的数据包。
         2、从 DatagramPacket 中获取数据内容的 ByteBuf，并创建一个 ByteBuffer 来计算校验和。
         3、重置并更新 CRC32 校验和计算器（crc32）的值，以计算数据的校验和。
         4、将计算得到的校验和值写入数据的前 4 个字节（使用小端字节序）。
         5、调用 ctx.write() 将带有校验和的数据写入通道，并使用给定的 ChannelPromise。
        */

        DatagramPacket datagramPacket = (DatagramPacket) msg;
        ByteBuf data = datagramPacket.content();
        ByteBuffer byteBuffer = data.nioBuffer(ChannelConfig.crc32Size,data.readableBytes()-ChannelConfig.crc32Size);
        crc32.reset();
        crc32.update(byteBuffer);
        long checksum = crc32.getValue();
        data.setIntLE(0, (int) checksum);
        //ByteBuf headByteBuf = ctx.alloc().ioBuffer(4);
        //headByteBuf.writeIntLE((int) checksum);
        //ByteBuf newByteBuf = Unpooled.wrappedBuffer(headByteBuf,data);
        //datagramPacket = datagramPacket.replace(newByteBuf);
        ctx.write(datagramPacket, promise);
    }
}
