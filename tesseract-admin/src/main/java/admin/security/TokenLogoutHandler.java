package admin.security;

import admin.entity.TesseractToken;
import admin.pojo.CommonResponseVO;
import admin.service.ITesseractTokenService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tesseract.exception.TesseractException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description: TODO-Eden.Lee
 * @author: 李明
 * @company: 朴新教育
 * @version:
 * @date: 2019/7/10 02:16
 */
@Component
public class TokenLogoutHandler implements LogoutHandler {

    @Autowired
    private ITesseractTokenService tokenService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            String token = request.getHeader("X-Token");
            if (StringUtils.isEmpty(token)) {
                throw new TesseractException("token为空");
            }
            QueryWrapper<TesseractToken> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(TesseractToken::getToken, token);
            tokenService.remove(queryWrapper);
            System.out.println("自定义登出:" + tokenService);
            response.getWriter().print(JSON.toJSONString(CommonResponseVO.SUCCESS));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
