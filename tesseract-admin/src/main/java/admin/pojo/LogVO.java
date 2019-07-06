package admin.pojo;

import admin.entity.TesseractLog;
import admin.entity.TesseractTrigger;
import lombok.Data;

import java.util.List;

@Data
public class LogVO {
    private PageVO pageInfo;
    private List<TesseractLog> logList;
}
