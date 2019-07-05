package admin.core.scheduler.router;

import admin.entity.TesseractExecutorTriggerLink;

import java.util.List;

public interface IScheduleRouter {
    TesseractExecutorTriggerLink routerExecutor(List<TesseractExecutorTriggerLink> tesseractExecutorTriggerLink);
}
