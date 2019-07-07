package admin.core.scheduler.router.impl;

import admin.core.scheduler.router.IScheduleRouter;
import admin.entity.TesseractExecutor;
import admin.entity.TesseractExecutorDetail;

import java.util.List;

/**
 * hash散列发送
 */
public class HashRouter implements IScheduleRouter {

    @Override
    public TesseractExecutorDetail routerExecutor(List<TesseractExecutorDetail> tesseractExecutorList) {

        return tesseractExecutorList.get(tesseractExecutorList.hashCode() % tesseractExecutorList.size());
    }
}
