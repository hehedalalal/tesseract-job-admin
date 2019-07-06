package admin.service.impl;

import admin.entity.TesseractToken;
import admin.entity.TesseractTrigger;
import admin.entity.TesseractUser;
import admin.mapper.TesseractUserMapper;
import admin.pojo.UserDO;
import admin.service.ITesseractTokenService;
import admin.service.ITesseractUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import tesseract.exception.TesseractException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static admin.constant.AdminConstant.USER_INVALID;
import static admin.constant.AdminConstant.USER_VALID;

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
    private static final String TOKEN_FORMATTER = "tessseract-%s-%s";
    /**
     * token过期时间，默认一天
     */
    private static final Integer TOKEN_EXPIRE_TIME = 24 * 60;
    @Autowired
    private ITesseractTokenService tokenService;
    private String defaultPassword = "666666";
    private String defaultPasswordMD5 = DigestUtils.md5DigestAsHex(defaultPassword.getBytes());

    @Override
    public String userLogin(UserDO userDO) {
        QueryWrapper<TesseractUser> queryWrapper = new QueryWrapper<>();
        String passwordMD5Str = DigestUtils.md5DigestAsHex(userDO.getPassword().getBytes());
        queryWrapper.lambda().eq(TesseractUser::getName, userDO.getUsername()).eq(TesseractUser::getPassword, passwordMD5Str);
        TesseractUser user = getOne(queryWrapper);
        if (user == null) {
            throw new TesseractException("用户名或密码错误");
        }
        LocalDateTime nowLocalDateTime = LocalDateTime.now();
        long nowTime = nowLocalDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        long expireTime = nowLocalDateTime.plusMinutes(TOKEN_EXPIRE_TIME).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        QueryWrapper<TesseractToken> tesseractTokenQueryWrapper = new QueryWrapper<>();
        TesseractToken tesseractToken = tokenService.getOne(tesseractTokenQueryWrapper);
        //如果token已存在
        if (tesseractToken != null) {
            tesseractToken.setUpdateTime(nowTime);
            tesseractToken.setExpireTime(expireTime);
            return tesseractToken.getToken();
        }
        String token = generateToken(user);
        tesseractToken = new TesseractToken();
        tesseractToken.setCreateTime(nowTime);
        tesseractToken.setUpdateTime(nowTime);
        tesseractToken.setExpireTime(expireTime);
        tesseractToken.setToken(token);
        tesseractToken.setUserId(user.getId());
        tesseractToken.setUserName(user.getName());
        tokenService.save(tesseractToken);
        return token;
    }

    @Override
    public void userLogout(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new TesseractException("token为空");
        }
        QueryWrapper<TesseractToken> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TesseractToken::getToken, token);
        tokenService.remove(queryWrapper);
    }

    @Override
    public IPage<TesseractUser> listByPage(Integer currentPage, Integer pageSize, TesseractUser condition) {
        Page<TesseractUser> page = new Page<>(currentPage, pageSize);
        QueryWrapper<TesseractUser> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<TesseractUser> lambda = queryWrapper.lambda();
        return page(page, queryWrapper);
    }

    @Override
    public void saveUser(TesseractUser tesseractUser) {
        long currentTimeMillis = System.currentTimeMillis();
        tesseractUser.setStatus(USER_VALID);
        tesseractUser.setUpdateTime(currentTimeMillis);
        tesseractUser.setPassword(defaultPasswordMD5);
        tesseractUser.setCreateTime(currentTimeMillis);
        this.save(tesseractUser);
    }

    @Override
    public void validUser(String userId) {
        QueryWrapper<TesseractUser> queryWrapper = new QueryWrapper<>();
        TesseractUser user = getOne(queryWrapper);
        if (user == null) {
            throw new TesseractException("用户不存在");
        }
        if (user.getStatus() == USER_VALID) {
            throw new TesseractException("用户已经是激活状态");
        }
        user.setStatus(USER_VALID);
        updateById(user);
    }

    @Override
    public void invalidUser(String userId) {
        QueryWrapper<TesseractUser> queryWrapper = new QueryWrapper<>();
        TesseractUser user = getOne(queryWrapper);
        if (user == null) {
            throw new TesseractException("用户不存在");
        }
        if (user.getStatus() == USER_INVALID) {
            throw new TesseractException("用户已经是禁用状态");
        }
        user.setStatus(USER_INVALID);
        updateById(user);
    }

    private String generateToken(TesseractUser user) {
        return String.format(TOKEN_FORMATTER, user.getName(), UUID.randomUUID().toString().replace("-", ""));
    }
}
