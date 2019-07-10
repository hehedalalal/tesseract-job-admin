package admin.core.scheduler.pool;

/**
 * 〈执行线程线程池〉
 *
 * @author nickel
 * @create 2019/6/23
 * @since 1.0.0
 */
public interface ISchedulerThreadPool {
    int blockGetAvailableThreadNum();

    void runJob(Runnable runnable);

    void shutdown();

    void init();

    void changeSize(Integer threadNum);
}
