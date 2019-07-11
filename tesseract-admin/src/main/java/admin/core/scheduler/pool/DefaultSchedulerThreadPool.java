package admin.core.scheduler.pool;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Iterator;
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
    private volatile int threadNum;
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

    @Override
    public void changeSize(Integer threadNum) {
        //如果大于现有线程数直接增加
        if (threadNum >= this.threadNum) {
            this.threadNum = threadNum;
            return;
        }
        //如果小于现有线程数则上锁清除
        synchronized (lock) {
            int num = this.threadNum - threadNum;
            //先尝试从可用线程数中清除
            if (availableWorkerList.size() >= num) {
                removeWorker(availableWorkerList, num);
                return;
            }
            //然后尝试从busy线程数中清除
            if (busyWorkerList.size() >= num) {
                removeWorker(busyWorkerList, num);
                return;
            }
            //如果两边都不足直接修改线程数
            this.threadNum = threadNum;
        }
    }

    private void removeWorker(List<WorkerThread> workerThreadList, Integer num) {
        Iterator<WorkerThread> iterator = workerThreadList.iterator();
        while (num-- > 0) {
            WorkerThread workerThread = iterator.next();
            //中断shutdown线程
            workerThread.interrupt();
            iterator.remove();
        }
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
            Thread.currentThread().interrupt();
        }

        @Override
        public void run() {
            while (!stop && !Thread.currentThread().isInterrupted()) {
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
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
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
            log.info("线程:{}退出", this.getName());
        }
    }
}
