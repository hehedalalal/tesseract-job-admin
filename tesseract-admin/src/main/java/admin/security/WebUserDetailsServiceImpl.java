package admin.security;

import admin.entity.TesseractUser;
import admin.pojo.WebUserDetail;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @description: TODO-Eden.Lee
 * @author: 李明
 * @company: 朴新教育
 * @version:
 * @date: 2019/7/9 14:28
 */
@Service("webUserDetailsService")
public class WebUserDetailsServiceImpl implements UserDetailsService {

    /**
     * 根据用户名登录
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO 数据库中获取用户密码，角色等信息

        TesseractUser tesseractUser = new TesseractUser();
        if (ObjectUtils.isEmpty(tesseractUser)) {
            throw new UsernameNotFoundException("用户登录，用户信息查询失败");
        }
        Set<String> roleSet = new HashSet<>();

        /**
         封装为框架使用的 userDetail {@link UserDetails}
         */
        WebUserDetail webUserDetail = new WebUserDetail();
        webUserDetail.setPassword(tesseractUser.getPassword());
        webUserDetail.setName(tesseractUser.getName());
        webUserDetail.setRoleSet(roleSet);

        webUserDetail.setPassword("admin");
        webUserDetail.setName("admin");
        roleSet.add("admin");
        webUserDetail.setRoleSet(roleSet);
        return webUserDetail;
    }
}
