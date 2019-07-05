package admin.service.impl;

import admin.entity.TesseractUser;
import admin.mapper.TesseractUserMapper;
import admin.pojo.UserDO;
import admin.service.ITesseractUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import tesseract.exception.TesseractException;

import java.util.UUID;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */
@Service
public class TesseractUserServiceImpl extends ServiceImpl<TesseractUserMapper, TesseractUser> implements ITesseractUserService {
    public static final String TOKEN_FORMATTER = "tessseract-%s-%s";

    @Override
    public String userLogin(UserDO userDO) {
        QueryWrapper<TesseractUser> queryWrapper = new QueryWrapper<>();
        String passwordMD5Str = DigestUtils.md5DigestAsHex(userDO.getPassword().getBytes());
        queryWrapper.lambda().eq(TesseractUser::getName, userDO.getUsername()).eq(TesseractUser::getPassword, passwordMD5Str);
        TesseractUser user = getOne(queryWrapper);
        if (user == null) {
            throw new TesseractException("用户名或密码错误");
        }
        String token = generateToken(user);
        user.setToken(token);
        user.setUpdateTime(System.currentTimeMillis());
        updateById(user);
        return token;
    }

    @Override
    public void userLogout(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new TesseractException("token为空");
        }
        QueryWrapper<TesseractUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TesseractUser::getToken, token);
        TesseractUser user = getOne(queryWrapper);
        if (user == null) {
            throw new TesseractException("用户没有token");
        }
        user.setUpdateTime(System.currentTimeMillis());
        user.setToken("");
        updateById(user);
    }

    public String generateToken(TesseractUser user) {
        return String.format(TOKEN_FORMATTER, user.getName(), UUID.randomUUID().toString().replace("-", ""));
    }
}
