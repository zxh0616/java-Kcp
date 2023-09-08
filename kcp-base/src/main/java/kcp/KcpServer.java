package kcp;

import com.backblaze.erasure.fec.Fec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.HashedWheelTimer;
import threadPool.IMessageExecutorPool;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by JinMiao
 * 2018/9/20.
 */
public class KcpServer {
    private IMessageExecutorPool iMessageExecutorPool;

    private Bootstrap bootstrap;
    private EventLoopGroup group;
    private List<Channel> localAddresss = new Vector<>();
    private IChannelManager channelManager;
    private HashedWheelTimer hashedWheelTimer;


    /**定时器线程工厂**/
    private static class TimerThreadFactory implements ThreadFactory
    {
        private AtomicInteger timeThreadName=new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r,"KcpServerTimerThread "+timeThreadName.addAndGet(1));
            return thread;
        }
    }

    //public void init(int workSize, KcpListener kcpListener, ChannelConfig channelConfig, int... ports) {
    //    DisruptorExecutorPool disruptorExecutorPool = new DisruptorExecutorPool();
    //    for (int i = 0; i < workSize; i++) {
    //        disruptorExecutorPool.createDisruptorProcessor("disruptorExecutorPool" + i);
    //    }
    //    init(disruptorExecutorPool, kcpListener, channelConfig, ports);
    //}


    public void init(KcpListener kcpListener, ChannelConfig channelConfig, int... ports) {
        if(channelConfig.isUseConvChannel()){
            int convIndex = 0;
            if(channelConfig.getFecAdapt()!=null){
                convIndex+= Fec.fecHeaderSizePlus2;
            }
            channelManager = new ServerConvChannelManager(convIndex);
        }else{
            channelManager = new ServerAddressChannelManager();
        }
        //定时器，用于在指定的时间间隔后执行任务。
        //这段代码创建了一个基于哈希轮算法的定时器，用于管理和触发定时任务。
        // 在构造函数中，它对输入参数进行验证、初始化轮和线程，还包括了一些关于内存泄漏和实例计数的逻辑。
        //threadFactory：用于创建线程的工厂。
        //tickDuration：每个“刻度”（tick）的时间间隔，以及该时间间隔的时间单位。
        //ticksPerWheel：哈希轮中的刻度数量。
        //leakDetection：是否启用内存泄漏检测。
        //maxPendingTimeouts：最大允许的等待超时数量。
        //taskExecutor：用于执行定时任务的执行器。
        hashedWheelTimer = new HashedWheelTimer(new TimerThreadFactory(),1, TimeUnit.MILLISECONDS);


        boolean epoll = Epoll.isAvailable();
        boolean kqueue = KQueue.isAvailable();
        this.iMessageExecutorPool = channelConfig.getiMessageExecutorPool();
        bootstrap = new Bootstrap();
        int cpuNum = Runtime.getRuntime().availableProcessors();
        int bindTimes = 1;
        if (epoll||kqueue) {
            //ADD SO_REUSEPORT ？ https://www.jianshu.com/p/61df929aa98b
            bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
            bindTimes = cpuNum;
        }
        Class<? extends Channel> channelClass = null;
        if(epoll){
            group = new EpollEventLoopGroup(cpuNum);
            channelClass = EpollDatagramChannel.class;
        }else if(kqueue){
            group = new KQueueEventLoopGroup(cpuNum);
            channelClass = KQueueDatagramChannel.class;
        }else{
            group = new NioEventLoopGroup(ports.length);
            channelClass = NioDatagramChannel.class;
        }

        bootstrap.channel(channelClass);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                ServerChannelHandler serverChannelHandler = new ServerChannelHandler(channelManager, channelConfig, iMessageExecutorPool, kcpListener,hashedWheelTimer);
                ChannelPipeline cp = ch.pipeline();
                if(channelConfig.isCrc32Check()){
                    Crc32Encode crc32Encode = new Crc32Encode();
                    Crc32Decode crc32Decode = new Crc32Decode();
                    //这里的crc32放在eventloop网络线程处理的，以后内核有丢包可以优化到单独的一个线程处理
                    cp.addLast(crc32Encode);
                    cp.addLast(crc32Decode);
                }
                cp.addLast(serverChannelHandler);
            }
        });

        //bootstrap.option(ChannelOption.SO_RCVBUF, 10*1024*1024);

   /*     SO_REUSEADDR 可以用来解决以下问题：
        端口重用： 当一个 socket 连接关闭后，它会在一段时间内进入 TIME_WAIT 状态，以确保所有延迟的数据片段都被正确处理。
                  这个状态会阻止其他 socket 绑定到相同的地址和端口，导致在某些情况下无法立即重用该地址。使用 SO_REUSEADDR 选项，您可以允许新的 socket 立即绑定到相同的地址和端口，即使之前的连接还处于 TIME_WAIT 状态。
        快速重启： 在一些网络服务中，特别是服务器应用，可能需要在关闭后迅速重启。
                  如果不启用 SO_REUSEADDR，重启时可能会因为之前的 socket 还在 TIME_WAIT 状态而导致无法绑定到相同的地址和端口。
      */
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);


        for (int port : ports) {
            for (int i = 0; i < bindTimes; i++) {
                ChannelFuture channelFuture = bootstrap.bind(port);
                Channel channel = channelFuture.channel();
                localAddresss.add(channel);
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));
    }

    public void stop() {
        localAddresss.forEach(
                channel -> channel.close()
        );
        channelManager.getAll().forEach(ukcp ->
                ukcp.close());
        if (iMessageExecutorPool != null) {
            iMessageExecutorPool.stop();
        }
        if(hashedWheelTimer!=null){
            hashedWheelTimer.stop();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    public IChannelManager getChannelManager() {
        return channelManager;
    }
}
