package threadPool.netty;

import io.netty.channel.EventLoop;
import threadPool.IMessageExecutor;
import threadPool.ITask;

/**
 * Created by JinMiao
 * 2020/11/24.
 */
public class NettyMessageExecutor implements IMessageExecutor {

    private EventLoop eventLoop;


    public NettyMessageExecutor(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public void execute(ITask iTask) {
        //用于检查当前线程是否处于事件循环中。
        if (eventLoop.inEventLoop()) {
            iTask.execute();
        } else {
            this.eventLoop.execute(() -> iTask.execute());
        }
    }
}
