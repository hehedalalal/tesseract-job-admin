package admin.pojo;

import admin.entity.TesseractGroup;
import lombok.Data;

import java.util.List;

@Data
public class GroupVO {
    private PageVO pageInfo;
    private List<TesseractGroup> groupList;
}
