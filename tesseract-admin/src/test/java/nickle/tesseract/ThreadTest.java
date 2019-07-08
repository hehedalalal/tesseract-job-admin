package nickle.tesseract;

public class ThreadTest {
    public static void main(String[] args) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {

                        this.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("123123");
            }
        };
        thread.start();
        thread.interrupt();
    }
}
