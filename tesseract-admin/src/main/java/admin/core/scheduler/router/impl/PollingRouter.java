package admin.core.scheduler.router.impl;

import admin.core.scheduler.router.IScheduleRouter;
import admin.entity.TesseractExecutorDetail;

import java.util.List;

public class PollingRouter implements IScheduleRouter {
    @Override
    public TesseractExecutorDetail routerExecutor(List<TesseractExecutorDetail> tesseractExecutorTriggerLink) {
        throw new RuntimeException("不支持的操作");
    }
}
