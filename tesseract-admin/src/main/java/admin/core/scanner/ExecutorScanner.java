package admin.core.scanner;

import tesseract.core.lifecycle.IThreadLifycycle;
import admin.entity.TesseractExecutorDetail;
import admin.service.ITesseractExecutorDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 失效执行器扫描线程
 *
 * @author nickle
 */
@Slf4j
public class ExecutorScanner extends Thread implements IThreadLifycycle {
    private ITesseractExecutorDetailService executorDetailService;
    private volatile boolean isStop = false;
    private Integer scanIntervalTime = 15 * 1000;

    public ExecutorScanner(ITesseractExecutorDetailService executorDetailService) {
        super("ExecutorScanner");
        this.executorDetailService = executorDetailService;
    }

    @Override
    public void run() {
        log.info("ExecutorScanner start");
        while (!isStop) {
            List<TesseractExecutorDetail> tesseractExecutorDetails = executorDetailService.listInvalid();
            if (!CollectionUtils.isEmpty(tesseractExecutorDetails)) {
                List<Integer> collect = tesseractExecutorDetails.stream().map(detail -> detail.getId()).collect(Collectors.toList());
                log.info("失效的机器:{}", tesseractExecutorDetails);
                executorDetailService.removeByIds(collect);
            }
            try {
                Thread.sleep(scanIntervalTime);
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void initThread() {

    }

    @Override
    public void startThread() {
        this.start();
    }

    @Override
    public void stopThread() {
        this.isStop = true;
        this.interrupt();
    }
}
