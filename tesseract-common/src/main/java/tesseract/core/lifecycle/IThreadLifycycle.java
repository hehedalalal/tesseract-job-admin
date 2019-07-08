package tesseract.core.lifecycle;

/**
 * @author nickle
 */
public interface IThreadLifycycle {
    void initThread();

    void startThread();

    void stopThread();
}
