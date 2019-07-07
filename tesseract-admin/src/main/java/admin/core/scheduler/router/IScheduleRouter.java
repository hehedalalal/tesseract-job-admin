package admin.core.scheduler.router;

import admin.entity.TesseractExecutor;
import admin.entity.TesseractExecutorDetail;

import java.util.List;

public interface IScheduleRouter {
    TesseractExecutorDetail routerExecutor(List<TesseractExecutorDetail> tesseractExecutorList);
}
