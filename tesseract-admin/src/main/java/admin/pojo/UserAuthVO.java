package admin.pojo;

import admin.entity.TesseractMenuResource;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 用户权限信息实体
 * @author: LeoLee
 * @company: ***
 * @version:
 * @date: 2019/7/10 10:09
 */
@Data
public class UserAuthVO implements Serializable {
    private List<String> roles;
    private List<TesseractMenuResource> menuList;
    private List<String> btnList;

    private String name;
    private String avatar;
    private String introduction;
}
