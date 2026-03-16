package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import untiy.exception.ErrorConfig;
import untiy.exception.EIException;
import untiy.entity.SysRole;
import untiy.entity.SysUserRole;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.service.AuthorService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AuthorServiceImpl implements AuthorService {
    //    权限相关
    @Autowired
    SysRoleMapper sysRoleMapper;
    @Autowired
    SysUserRoleMapper sysUserRoleMapper;
/*
* Spring Security 的认证流程最终需要的就是 Collection<? extends GrantedAuthority>
因此返回的权限集合不再是收集列表而是对应的List<GrantedAuthority>
* 返回总体的父类,复用性增强
* */
    @Override
    public List<GrantedAuthority> getAuthoritiesByUserId(Long userId) {
//        检查id是否为空
        if (Objects.isNull(userId)){
            throw new EIException(ErrorConfig.Author_ID_CODE,ErrorConfig.Author_ID_MSG);
        }
//        查询角色对应角色id
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, userId));
//        检查集合, 空和非空处理
        if ((userRoles.isEmpty())){
            return Collections.emptyList();
        }
//        角色信息对应角色id收集 list类型
        List<Long> list = userRoles.stream()
//                处理为流函数
                .map(SysUserRole::getRoleId)
//                根据角色编码得到角色信息ID
                .collect(Collectors.toList());
//ID查找对应角色
        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getId, list)
                        .eq(SysRole::getStatus, 1)
        );
        //        处理GrantedAuthority并返回
        List<GrantedAuthority> collect = roles.stream()
                .map(sysRole -> new SimpleGrantedAuthority("ROLE_" + sysRole.getRoleCode()))
                .collect(Collectors.toList());
        return collect;
    }
}
