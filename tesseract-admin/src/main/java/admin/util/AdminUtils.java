package admin.util;

import admin.entity.TesseractTrigger;
import admin.service.ITesseractTriggerService;

import java.util.List;


public class AdminUtils {
    public static void updateTriggerStatus(ITesseractTriggerService tesseractTriggerService, List<TesseractTrigger> triggerList, Integer status) {
        triggerList.parallelStream().forEach(trigger -> {
            trigger.setStatus(status);
        });
        tesseractTriggerService.updateBatchById(triggerList);
    }
}
