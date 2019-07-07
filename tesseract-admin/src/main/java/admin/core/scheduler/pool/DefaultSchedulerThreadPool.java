package admin.core.scheduler.pool;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 〈默认执行器线程池〉
 *
 * @author nickel
 * @create 2019/6/23
 * @since 1.0.0
 */
@Slf4j
public class DefaultSchedulerThreadPool implements ISchedulerThreadPool {
    private final int threadNum;
    private final AtomicInteger atomicInteger = new AtomicInteger(0);
    private final Object lock = new Object();
    private volatile boolean isStop = false;

    public DefaultSchedulerThreadPool(int threadNum) {
        this.threadNum = threadNum;
    }

    private List<WorkerThread> availableWorkerList = Collections.synchronizedList(Lists.newArrayList());
    private List<WorkerThread> busyWorkerList = Collections.synchronizedList(Lists.newArrayList());

    @Override
    public int blockGetAvailableThreadNum() {
        if (isStop) {
            log.error("线程池已关闭");
            return 0;
        }
        int count;
        synchronized (lock) {
            while (busyWorkerList.size() == threadNum) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                }
            }
            count = threadNum - busyWorkerList.size();
        }
        return count;
    }

    @Override
    public void runJob(Runnable runnable) {
        if (isStop) {
            log.error("线程池已关闭");
            return;
        }
        synchronized (lock) {
            //确保没有超出可用线程
            while (busyWorkerList.size() == threadNum) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                }
            }
            WorkerThread worker;
            if (availableWorkerList.size() > 0) {
                worker = availableWorkerList.remove(0);
            } else {
                worker = new WorkerThread(availableWorkerList, busyWorkerList);
                worker.start();
                busyWorkerList.add(worker);
            }
            worker.setRunnable(runnable);
        }
    }

    @Override
    public void shutdown() {
        this.isStop = true;
        availableWorkerList.forEach(workerThread -> workerThread.stopThread());
        busyWorkerList.forEach(workerThread -> workerThread.stopThread());
    }

    @Override
    public void init() {

    }

    private class WorkerThread extends Thread {
        private volatile Runnable runnable;
        private volatile boolean stop = false;
        private List<WorkerThread> availableWorkerList;
        private List<WorkerThread> busyWorkerList;

        public WorkerThread(List<WorkerThread> availableWorkerList, List<WorkerThread> busyWorkerList) {
            super(String.format("tesseract-work-thread-%d", atomicInteger.incrementAndGet()));
            this.availableWorkerList = availableWorkerList;
            this.busyWorkerList = busyWorkerList;
        }

        public void setRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        public void stopThread() {
            log.info("work thread stop");
            this.stop = true;
        }

        @Override
        public void run() {
            while (!stop) {
                if (runnable == null) {
                    //响应是否关闭
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                try {
                    runnable.run();
                } catch (Exception e) {
                } finally {
                    runnable = null;
                    //移动自己到空闲线程队列
                    busyWorkerList.remove(this);
                    availableWorkerList.add(this);
                    //通知阻塞的线程
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            }
        }
    }
}
