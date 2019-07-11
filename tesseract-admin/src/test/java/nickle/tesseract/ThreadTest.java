package nickle.tesseract;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadTest {
    public static void main(String[] args) throws Exception {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 10,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));
        for (int i = 0; i < 25; i++) {
            int activeCount = threadPoolExecutor.getActiveCount();
            int corePoolSize = threadPoolExecutor.getCorePoolSize();
            int maximumPoolSize = threadPoolExecutor.getMaximumPoolSize();
            int poolSize = threadPoolExecutor.getPoolSize();
            int size = threadPoolExecutor.getQueue().size();
            System.out.println("activeCount:" + activeCount);
            System.out.println("corePoolSize:" + corePoolSize);
            System.out.println("maximumPoolSize:" + maximumPoolSize);
            System.out.println("poolSize:" + poolSize);
            System.out.println("size:" + size);
            Thread.sleep(2000);

            threadPoolExecutor.execute(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {

                }
            });

        }
    }
}
