package admin.service;

import admin.entity.TesseractTrigger;
import admin.entity.TesseractUser;
import admin.pojo.PageVO;
import admin.pojo.UserDO;
import admin.pojo.UserVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */
public interface ITesseractUserService extends IService<TesseractUser> {
    String userLogin(UserDO userDO);

    void userLogout(String token);

    IPage<TesseractUser> listByPage(Integer currentPage, Integer pageSize, TesseractUser condition);
}
