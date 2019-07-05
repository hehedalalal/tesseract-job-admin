package admin.pojo;

import admin.entity.TesseractTrigger;
import lombok.Data;

import java.util.List;

@Data
public class TriggerVO {
    private PageVO pageInfo;
    private List<TesseractTrigger> triggerList;
}
