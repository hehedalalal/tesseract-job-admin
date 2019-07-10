package admin.service;

import admin.entity.TesseractUser;
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

    IPage<TesseractUser> listByPage(Integer currentPage, Integer pageSize, TesseractUser condition,
                                    Long startCreateTime,
                                    Long endCreateTime);

    void saveOrUpdateUser(TesseractUser tesseractUser);

    void passwordRevert(Integer userId);

    void validUser(Integer userId);

    void invalidUser(Integer userId);

    Collection<Integer> statisticsUser();

    void deleteUser(Integer userId);
}
