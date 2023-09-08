package tets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * 测试延迟的例子
 * Created by JinMiao
 * 2018/11/2.
 */
public class ZjqTest {

    public static void main(String[] args) {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer(6);
        byteBuf.writeBytes("已经接收到".getBytes());


        int readableBytes = byteBuf.readableBytes();
        byte[] contentBytes = new byte[readableBytes];
        byteBuf.readBytes(contentBytes);
        String content = new String(contentBytes);
        System.out.println("接收到的信息：" + content);
    }
}


