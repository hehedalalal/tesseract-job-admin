package admin.service;

import admin.entity.TesseractUser;
import admin.pojo.UserAuthVO;
import admin.pojo.UserDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;

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

    void saveUser(TesseractUser tesseractUser);

    void validUser(String userId);

    void invalidUser(String userId);

    Collection<Integer> statisticsUser();
    /**
     * 根据Token获取用户权限信息
     * @param token
     * @return: admin.pojo.UserAuthVO
     * @author: LeoLee
     * @date: 2019/7/10 10:16
     */
    UserAuthVO getUserAuthInfo(String token);
}
