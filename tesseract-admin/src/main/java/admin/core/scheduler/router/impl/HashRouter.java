package admin.core.scheduler.router.impl;

import admin.core.scheduler.router.IScheduleRouter;
import admin.entity.TesseractExecutorTriggerLink;

import java.util.Arrays;
import java.util.List;

/**
 * hash散列发送
 */
public class HashRouter implements IScheduleRouter {

    @Override
    public List<TesseractExecutorTriggerLink> routerExecutor(List<TesseractExecutorTriggerLink> tesseractExecutorTriggerLink) {

        return Arrays.asList(tesseractExecutorTriggerLink.get(tesseractExecutorTriggerLink.hashCode() % tesseractExecutorTriggerLink.size()));
    }
}
