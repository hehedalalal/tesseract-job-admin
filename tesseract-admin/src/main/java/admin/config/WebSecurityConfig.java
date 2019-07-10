package admin.config;

import admin.security.TokenAuthenticationFilter;
import admin.security.TokenLogoutHandler;
import admin.service.ITesseractUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.*;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.Iterator;

/**
 * @description: 安全配置类
 * @author: LeoLee
 * @company: ***
 * @version:
 * @date: 2019/7/9 14:03
 */
@Slf4j
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private ITesseractUserService tesseractUserService;
    @Autowired
    private TokenAuthenticationFilter tokenAuthenticationFilter;
    @Autowired
    private TokenLogoutHandler tokenLogoutHandler;

    /**
     * http安全配置
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 开启跨域共享
                .cors().and()
                // 跨域伪造请求限制.无效
                .csrf().disable()
                // 开启授权认证
                .authorizeRequests()
                //.antMatchers("/tesseract-user/**").authenticated()
                .antMatchers("/tesseract-user/userList").hasAuthority("admin")
                .antMatchers("/tesseract-user/getUserAuthInfo").hasAnyAuthority("admin")
                // OPTIONS预检请求不处理
                .antMatchers("/tesseract-user/login").permitAll()
                .antMatchers("/tesseract-user/register").permitAll()
                // 其它请求随意访问
                .anyRequest().authenticated().and()
                // 基于token，所以不需要session。无状态
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .exceptionHandling().and()
                //自定义401返回
                //.authenticationEntryPoint(new TokenAuthenticationEntryPoint()).and()
                .logout().logoutUrl("/tesseract-user/logout").addLogoutHandler(tokenLogoutHandler).permitAll().and()
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }


    /**
     * 配置登录验证，可以做加密处理
     * @param authenticationManagerBuilder
     * @throws Exception
     */
    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                // 设置UserDetailsService
                .userDetailsService(userDetailsService);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(NoOpPasswordEncoder.getInstance());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    /**
     * 密码生成策略.
     *
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * cors跨越
     *
     * @return
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);
        corsConfiguration.addExposedHeader("access-control-allow-methods");
        corsConfiguration.addExposedHeader("access-control-allow-headers");
        corsConfiguration.addExposedHeader("access-control-allow-origin");
        corsConfiguration.addExposedHeader("access-control-max-age");
        corsConfiguration.addExposedHeader("X-Frame-Options");

        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/", corsConfiguration);
        return configurationSource;
    }
}
