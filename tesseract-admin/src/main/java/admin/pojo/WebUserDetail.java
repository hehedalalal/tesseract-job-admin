package admin.pojo;

import admin.entity.TesseractUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description: Security使用用户类
 * @author: 李明
 * @company: 朴新教育
 * @version:
 * @date: 2019/7/9 15:25
 */
public class WebUserDetail extends TesseractUser implements UserDetails {

    /**
     * 角色
     */
    private Set<String> roleSet;

    /**
     * 获取权限信息
     *
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        /**
         * 将角色信息封装为框架要求格式
         */
        if (roleSet == null) {
            return null;
        }
        return roleSet.stream().map(
                s -> new SimpleGrantedAuthority(s)
        ).collect(Collectors.toSet());
    }

    public Set<String> getRoleSet() {
        return roleSet;
    }

    public void setRoleSet(Set<String> roleSet) {
        this.roleSet = roleSet;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
