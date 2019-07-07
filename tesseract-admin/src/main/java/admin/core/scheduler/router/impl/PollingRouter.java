package admin.core.scheduler.router.impl;

import admin.core.scheduler.router.IScheduleRouter;
import admin.entity.TesseractExecutor;
import admin.entity.TesseractExecutorDetail;

import java.util.List;

public class PollingRouter implements IScheduleRouter {
    @Override
    public TesseractExecutorDetail routerExecutor(List<TesseractExecutorDetail> tesseractExecutorTriggerLink) {
        return null;
    }
}
