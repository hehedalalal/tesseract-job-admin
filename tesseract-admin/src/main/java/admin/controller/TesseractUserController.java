package admin.controller;


import admin.entity.TesseractUser;
import admin.pojo.CommonResponseVO;
import admin.pojo.PageVO;
import admin.pojo.UserDO;
import admin.pojo.UserVO;
import admin.service.ITesseractUserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashMap;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */
@RestController
@RequestMapping("/tesseract-user")
@Validated
public class TesseractUserController {
    @Autowired
    private ITesseractUserService tesseractUserService;

    @RequestMapping("/login")
    public CommonResponseVO login(@Validated @RequestBody UserDO userDO) {
        String token = tesseractUserService.userLogin(userDO);
//        roles: ['admin'],
//        introduction: 'I am a super administrator',
//        avatar: 'https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif',
//        name: 'Super Admin'
        HashMap<String, Object> hashMap = Maps.newHashMap();
        hashMap.put("roles", Arrays.asList("admin"));
        hashMap.put("introduction", "I am a super administrator");
        hashMap.put("avatar", "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        hashMap.put("name", "Super Admin");
        hashMap.put("token", token);
        return new CommonResponseVO(CommonResponseVO.SUCCESS_STATUS, hashMap);
    }

    @RequestMapping("/logout")
    public CommonResponseVO logout(HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getHeader("X-Token");
        tesseractUserService.userLogout(token);
        return CommonResponseVO.SUCCESS;
    }

    @RequestMapping("/userList")
    public CommonResponseVO userList(@NotNull @Min(1) Integer currentPage
            , @NotNull @Min(1) @Max(50) Integer pageSize, TesseractUser condition,
                                     Long startCreateTime,
                                     Long endCreateTime) {
        IPage<TesseractUser> userIPage = tesseractUserService.listByPage(currentPage, pageSize
                , condition, startCreateTime, endCreateTime);
        UserVO userVO = new UserVO();
        PageVO pageVO = new PageVO();
        pageVO.setCurrentPage(userIPage.getCurrent());
        pageVO.setPageSize(userIPage.getSize());
        pageVO.setTotal(userIPage.getTotal());
        userVO.setPageInfo(pageVO);
        userVO.setUserList(userIPage.getRecords());
        return CommonResponseVO.success(userVO);
    }

    @RequestMapping("/addUser")
    public CommonResponseVO addUser(@Validated @RequestBody TesseractUser tesseractUser) throws Exception {
        tesseractUserService.saveOrUpdateUser(tesseractUser);
        return CommonResponseVO.SUCCESS;
    }

    @RequestMapping("/passwordRevert")
    public CommonResponseVO passwordRevert(@NotNull Integer userId) throws Exception {
        tesseractUserService.passwordRevert(userId);
        return CommonResponseVO.SUCCESS;
    }

    @RequestMapping("/validUser")
    public CommonResponseVO validUser(@NotNull Integer userId) throws Exception {
        tesseractUserService.validUser(userId);
        return CommonResponseVO.SUCCESS;
    }

    @RequestMapping("/invalidUser")
    public CommonResponseVO invalidUser(@NotNull Integer userId) throws Exception {
        tesseractUserService.invalidUser(userId);
        return CommonResponseVO.SUCCESS;
    }

    @RequestMapping("/deleteUser")
    public CommonResponseVO deleteUser(@NotNull Integer userId) throws Exception {
        tesseractUserService.deleteUser(userId);
        return CommonResponseVO.SUCCESS;
    }

    @RequestMapping("/getUserCount")
    private CommonResponseVO getUserCount() {
        return CommonResponseVO.success(tesseractUserService.count());
    }

    /**
     * 统计最近七天的数据
     *
     * @return
     */
    @RequestMapping("/statisticsUser")
    private CommonResponseVO statisticsUser() {
        return CommonResponseVO.success(tesseractUserService.statisticsUser());
    }
}

