package kcp;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import threadPool.IMessageExecutor;
import threadPool.ITask;

import java.util.concurrent.TimeUnit;

/**
 * Created by JinMiao
 * 2018/10/24.
 */
public class ScheduleTask implements ITask, Runnable, TimerTask {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(ScheduleTask.class);
    private final IMessageExecutor messageExecutor;

    private final Ukcp ukcp;

    private final HashedWheelTimer hashedWheelTimer;

    public ScheduleTask(IMessageExecutor messageExecutor, Ukcp ukcp, HashedWheelTimer hashedWheelTimer) {
        this.messageExecutor = messageExecutor;
        this.ukcp = ukcp;
        this.hashedWheelTimer = hashedWheelTimer;
    }

    //flush策略
    //1,WriteTask  在send调用后检查缓冲区，如果可以发送直接调用update得到时间并存在ukcp内
    //2， ScheduleTask  定时任务到了检查ukcp的时间和自己的定时，如果可以发送则直接发送  时间延后则重新定时，
    //定时任务发送成功后检测缓冲区  是否触发发送事件
    //3，ReadTask 读事件触发后 检测写缓冲区触发写事件
    //问题: 精准大量的flush触发会导致ack重复发送   流量增大？  不会的 ack只会发送一次
    @Override
    public void execute() {
        try {
            final Ukcp ukcp = this.ukcp;
            long now = System.currentTimeMillis();
            //判断连接是否关闭
            if (ukcp.getTimeoutMillis() != 0 && now - ukcp.getTimeoutMillis() > ukcp.getLastRecieveTime()) {
                if (log.isDebugEnabled()) {
                    log.debug("{} ScheduleTask :超时退出", this);
                }
                ukcp.internalClose();
            }
            if (!ukcp.isActive()) {
                return;
            }
            long timeLeft = ukcp.getTsUpdate() - now;
            if (log.isDebugEnabled()) {
                log.debug("{} ScheduleTask : tsUpdate={}, now={}, timeLeft={}", this, ukcp.getTsUpdate(), now, timeLeft);
            }
            //判断执行时间是否到了
            if (timeLeft > 0) {
                hashedWheelTimer.newTimeout(this,timeLeft, TimeUnit.MILLISECONDS);
                return;
            }
            long next = ukcp.flush(now);
            if (log.isDebugEnabled()) {
                log.debug("{} ScheduleTask : next={}", this, next);
            }
            hashedWheelTimer.newTimeout(this, next, TimeUnit.MILLISECONDS);
            //检测写缓冲区 如果能写则触发写事件
            if (!ukcp.getWriteBuffer().isEmpty() && ukcp.canSend(false))
            {
                ukcp.notifyWriteEvent();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        this.messageExecutor.execute(this);
    }

    @Override
    public void run(Timeout timeout) {
        run();
    }
}
