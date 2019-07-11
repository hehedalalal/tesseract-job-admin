package admin.core.scheduler.router.impl;

import admin.core.scheduler.router.IScheduleRouter;
import admin.entity.TesseractExecutorDetail;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
public class LoadFactorRouter implements IScheduleRouter {
    @Override
    public TesseractExecutorDetail routerExecutor(List<TesseractExecutorDetail> tesseractExecutorDetailList) {
        TesseractExecutorDetail tesseractExecutorDetail;
        if (tesseractExecutorDetailList.size() == 1) {
            tesseractExecutorDetail = tesseractExecutorDetailList.get(0);
        } else {
            Collections.sort(tesseractExecutorDetailList, (o1, o2) -> {
                if (o1.getLoadFactor() > o2.getLoadFactor()) {
                    return 1;
                }
                if (o1.getLoadFactor() < o2.getLoadFactor()) {
                    return -1;
                }
                return 0;
            });
            tesseractExecutorDetail = tesseractExecutorDetailList.get(0);
        }
        log.info("排序结果:{}", tesseractExecutorDetailList);
        return tesseractExecutorDetail;
    }
}
