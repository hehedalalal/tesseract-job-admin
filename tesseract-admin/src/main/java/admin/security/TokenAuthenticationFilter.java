package admin.security;
import admin.config.WebSecurityConfig;
import admin.entity.TesseractUser;
import admin.pojo.CommonResponseVO;
import admin.pojo.UserAuthVO;
import admin.service.ITesseractUserService;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: TODO-Eden.Lee
 * @author: LeoLee
 * @company: ***
 * @version:
 * @date: 2019/7/9 18:07
 */
@Component
public class TokenAuthenticationFilter  extends OncePerRequestFilter {



    //        @Autowired
//        RedisTemplate<String, String> redisTemplate;
    String tokenHeader = "X-Token";
    //String tokenHead = "Bearer ";
    // String tokenHeader = "Authorization";
    @Autowired
    private ITesseractUserService tesseractUserService;
    @Autowired
    private UserDetailsService webUserDetailsService;



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 将 ServletRequest 转换为 HttpServletRequest 才能拿到请求头中的 token
        String token = request.getHeader(this.tokenHeader);
        String servletPath = request.getServletPath();
        try{
            if (!StringUtils.isEmpty(token)) {
                // final String authToken = authHeader.substring(tokenHead.length()); // The part after "Bearer "
                // if (authToken != null && redisTemplate.hasKey(authToken)) {
                //String username = redisTemplate.opsForValue().get(authToken);
                UserAuthVO userAuthVO = tesseractUserService.getUserAuthInfo(token);
                // 如果上面解析 token 成功并且拿到了 username 并且本次会话的权限还未被写入
                if (userAuthVO != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    UserDetails userDetails = webUserDetailsService.loadUserByUsername(userAuthVO.getName());
                    // 权限
                    List<GrantedAuthority> permissions = new ArrayList<>();
                    // TODO: 查询用户权限,默认无权限
                    //可以校验token和username是否有效，目前由于token对应username存在redis，都以默认都是有效的
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(
                            request));
                    //验证正常,生成authentication
                    logger.info("authenticated user " + userAuthVO.getName() + ", setting security context");
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }finally {
            filterChain.doFilter(request, response);
        }
    }
}
