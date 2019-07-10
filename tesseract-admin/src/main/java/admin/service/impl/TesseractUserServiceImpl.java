package admin.service.impl;

import admin.entity.TesseractToken;
import admin.entity.TesseractUser;
import admin.mapper.TesseractUserMapper;
import admin.pojo.StatisticsLogDO;
import admin.pojo.UserDO;
import admin.service.ITesseractTokenService;
import admin.service.ITesseractUserService;
import admin.util.AdminUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import tesseract.exception.TesseractException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
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
@Slf4j
public class TesseractUserServiceImpl extends ServiceImpl<TesseractUserMapper, TesseractUser> implements ITesseractUserService {
    private static final String TOKEN_FORMATTER = "tessseract-%s-%s";
    /**
     * token过期时间，默认2小时
     */
    private static final Integer TOKEN_EXPIRE_TIME = 2 * 60;
    @Autowired
    private ITesseractTokenService tokenService;
    private String defaultPassword = "666666";
    private String defaultPasswordMD5 = DigestUtils.md5DigestAsHex(defaultPassword.getBytes());
    private int statisticsDays = 7;

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
        QueryWrapper<TesseractToken> tesseractTokenQueryWrapper = new QueryWrapper<>();
        tesseractTokenQueryWrapper.lambda().eq(TesseractToken::getUserId, user.getId());
        TesseractToken tesseractToken = tokenService.getOne(tesseractTokenQueryWrapper);
        //如果token已存在
        if (tesseractToken != null) {
            //检测是否过期
            Long expireTime = tesseractToken.getExpireTime();
            if (nowTime < expireTime) {
                tesseractToken.setToken(generateToken(user));
                tesseractToken.setUpdateTime(nowTime);
                tokenService.updateById(tesseractToken);
            }
            return tesseractToken.getToken();
        }
        //创建新的token
        long expireTime = nowLocalDateTime.plusMinutes(TOKEN_EXPIRE_TIME).toInstant(ZoneOffset.of("+8")).toEpochMilli();
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
    public IPage<TesseractUser> listByPage(Integer currentPage, Integer pageSize, TesseractUser condition,
                                           Long startCreateTime,
                                           Long endCreateTime) {
        Page<TesseractUser> page = new Page<>(currentPage, pageSize);
        QueryWrapper<TesseractUser> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<TesseractUser> lambda = queryWrapper.lambda();
        //日期
        if (startCreateTime != null) {
            lambda.ge(TesseractUser::getCreateTime, startCreateTime);
        }

        if (endCreateTime != null) {
            lambda.le(TesseractUser::getCreateTime, endCreateTime);
        }

        //其他
        AdminUtils.buildCondition(queryWrapper, condition);
        return page(page, queryWrapper);
    }

    @Override
    public void saveOrUpdateUser(TesseractUser tesseractUser) {
        long currentTimeMillis = System.currentTimeMillis();
        Integer id = tesseractUser.getId();
        if (id != null) {
            tesseractUser.setUpdateTime(currentTimeMillis);
            updateById(tesseractUser);
            return;
        }
        tesseractUser.setStatus(USER_VALID);
        tesseractUser.setUpdateTime(currentTimeMillis);
        tesseractUser.setPassword(defaultPasswordMD5);
        tesseractUser.setCreateTime(currentTimeMillis);
        this.save(tesseractUser);
    }

    @Override
    public void passwordRevert(Integer userId) {
        TesseractUser user = getById(userId);
        if (user == null) {
            throw new TesseractException("用户为空");
        }
        user.setPassword(defaultPasswordMD5);
        user.setUpdateTime(System.currentTimeMillis());
        updateById(user);
    }

    @Override
    public void validUser(Integer userId) {

        TesseractUser user = getById(userId);
        if (user == null) {
            throw new TesseractException("用户不存在");
        }
        if (user.getStatus().equals(USER_VALID)) {
            throw new TesseractException("用户已经是激活状态");
        }
        user.setStatus(USER_VALID);
        updateById(user);
    }

    @Override
    public void invalidUser(Integer userId) {
        TesseractUser user = getById(userId);
        if (user == null) {
            throw new TesseractException("用户不存在");
        }
        if (user.getStatus().equals(USER_INVALID)) {
            throw new TesseractException("用户已经是禁用状态");
        }
        user.setStatus(USER_INVALID);
        updateById(user);
    }

    @Override
    public Collection<Integer> statisticsUser() {
        LocalDate now = LocalDate.now();
        long startTime = now.minus(6, ChronoUnit.DAYS).atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        long endTime = now.plus(1, ChronoUnit.DAYS).atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        List<StatisticsLogDO> statisticsLogDOList = tokenService.statisticsActiveUser(startTime, endTime);
        return AdminUtils.buildStatisticsList(statisticsLogDOList, statisticsDays);
    }

    @Override
    public void deleteUser(Integer userId) {
        TesseractUser user = getById(userId);
        if (user == null) {
            throw new TesseractException("用户不存在");
        }
        removeById(userId);
    }

    private String generateToken(TesseractUser user) {
        return String.format(TOKEN_FORMATTER, user.getName(), UUID.randomUUID().toString().replace("-", ""));
    }
}
