package admin.pojo;

import admin.entity.TesseractUser;
import lombok.Data;

import java.util.List;

@Data
public class UserVO {
    private PageVO pageInfo;
    private List<TesseractUser> userList;
}
