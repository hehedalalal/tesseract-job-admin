package admin.security;

import admin.pojo.CommonResponseVO;
import com.alibaba.fastjson.JSON;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.jws.WebResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @description: TODO-Eden.Lee
 * @author: 李明
 * @company: 朴新教育
 * @version:
 * @date: 2019/7/9 19:54
 */
public class TokenAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse response, AuthenticationException e) throws IOException {
        //设置返回状态码401
        response.setStatus(401);
        response.setContentType("text/html; charset=utf-8");
        //writer 输出
        final PrintWriter writer = response.getWriter();
        try {
            writer.write(JSON.toJSONString(CommonResponseVO.fail("登录失效")));
        } finally {
            //别忘了close
            writer.close();
        }
    }
}
