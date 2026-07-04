package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import untiy.entity.SysMenu;
import untiy.entity.SysRole;
import untiy.entity.SysRoleMenu;
import untiy.entity.SysUserRole;
import untiy.entity.vo.MenuTreeResultVO;
import untiy.entity.vo.MenuTreeVO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.mapper.SysMenuMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysRoleMenuMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.LoginUserDetails;
import untiy.security.MenuCacheHelper;
import untiy.security.MenuTreeHelper;
import untiy.service.SysMenuService;
import untiy.utils.MPUtil;
import untiy.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private static final int MENU_TYPE_PAGE = 2;
    private static final int MENU_TYPE_BUTTON = 3;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public MenuTreeResultVO getMenuTreeForCurrentUser() {
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        Long userId = user.getUserId();

        MenuTreeResultVO cached = MenuCacheHelper.get(redisTemplate, userId);
        if (cached != null) {
            return cached;
        }

        MenuTreeResultVO result = buildMenuTreeForUser(userId);
        MenuCacheHelper.put(redisTemplate, userId, result);
        return result;
    }

    private MenuTreeResultVO buildMenuTreeForUser(Long userId) {
        MenuTreeResultVO result = new MenuTreeResultVO();

        List<Long> roleIds = sysUserRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .distinct()
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return result;
        }

        List<Long> menuIds = sysRoleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());

        if (menuIds.isEmpty()) {
            return result;
        }

        List<SysMenu> assignedMenus = list(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getStatus, 1));

        Map<Long, SysMenu> allActive = list(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus, 1))
                .stream()
                .collect(Collectors.toMap(SysMenu::getId, m -> m, (a, b) -> a));

        List<SysMenu> withAncestors = MenuTreeHelper.expandWithAncestors(assignedMenus, allActive);

        result.getPermissions().addAll(MenuTreeHelper.collectPermissionCodes(assignedMenus));
        List<MenuTreeVO> tree = MenuTreeHelper.buildTree(withAncestors, 0L);
        result.setTree(tree);
        return result;
    }

    @Override
    public IPage<SysMenu> pageQuery(Map<String, Object> param, SysMenu query) {
        Page<SysMenu> page = MPUtil.getPage(param);
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();

        if (query != null) {
            if (StringUtils.isNotBlank(query.getMenuName())) {
                wrapper.like(SysMenu::getMenuName, query.getMenuName());
            }
            if (query.getMenuType() != null) {
                wrapper.eq(SysMenu::getMenuType, query.getMenuType());
            }
        }

        wrapper.orderByAsc(SysMenu::getSort).orderByAsc(SysMenu::getId);
        return baseMapper.selectPage(page, wrapper);
    }

    @Transactional
    @Override
    public void saveMenu(SysMenu menu) {
        if (menu == null) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, ErrorConfig.BAD_REQUEST_MSG);
        }

        normalizeParentId(menu);
        validateParentExists(menu.getParentId());
        assertNoCycle(menu.getId(), menu.getParentId());
        assertSiblingNameUnique(menu);
        validateMenuTypeFields(menu);

        if (menu.getId() != null) {
            SysMenu existing = getById(menu.getId());
            if (existing == null) {
                throw new EIException(ErrorConfig.MENU_NOT_FOUND_CODE, ErrorConfig.MENU_NOT_FOUND_MSG);
            }
            assertMenuNotBoundByHigherRole(menu.getId());
            if (menu.getCreateTime() == null) {
                menu.setCreateTime(existing.getCreateTime());
            }
            updateById(menu);
        } else {
            if (menu.getStatus() == null) {
                menu.setStatus(1);
            }
            if (menu.getSort() == null) {
                menu.setSort(0);
            }
            menu.setCreateTime(LocalDateTime.now());
            save(menu);
        }

        MenuCacheHelper.evictAll(redisTemplate);
    }

    @Transactional
    @Override
    public void deleteMenu(Long id) {
        if (id == null) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, ErrorConfig.BAD_REQUEST_MSG);
        }
        SysMenu menu = getById(id);
        if (menu == null) {
            throw new EIException(ErrorConfig.MENU_NOT_FOUND_CODE, ErrorConfig.MENU_NOT_FOUND_MSG);
        }

        long childCount = count(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new EIException(ErrorConfig.MENU_HAS_CHILDREN_CODE, ErrorConfig.MENU_HAS_CHILDREN_MSG);
        }

        assertMenuNotBoundByHigherRole(id);

        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getMenuId, id));
        removeById(id);
        MenuCacheHelper.evictAll(redisTemplate);
        log.info("菜单 id={} 已删除并清理角色关联", id);
    }

    private void normalizeParentId(SysMenu menu) {
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
    }

    private void validateParentExists(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        if (getById(parentId) == null) {
            throw new EIException(ErrorConfig.MENU_PARENT_NOT_FOUND_CODE, ErrorConfig.MENU_PARENT_NOT_FOUND_MSG);
        }
    }

    private void assertNoCycle(Long id, Long parentId) {
        if (id == null || parentId == null || parentId == 0L) {
            return;
        }
        if (Objects.equals(id, parentId)) {
            throw new EIException(ErrorConfig.MENU_CYCLE_CODE, ErrorConfig.MENU_CYCLE_MSG);
        }
        Set<Long> visited = new HashSet<>();
        Long current = parentId;
        while (current != null && current != 0L) {
            if (Objects.equals(current, id)) {
                throw new EIException(ErrorConfig.MENU_CYCLE_CODE, ErrorConfig.MENU_CYCLE_MSG);
            }
            if (!visited.add(current)) {
                break;
            }
            SysMenu parent = getById(current);
            if (parent == null) {
                break;
            }
            current = parent.getParentId();
        }
    }

    private void assertSiblingNameUnique(SysMenu menu) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, menu.getParentId())
                .eq(SysMenu::getMenuName, menu.getMenuName());
        if (menu.getId() != null) {
            wrapper.ne(SysMenu::getId, menu.getId());
        }
        if (count(wrapper) > 0) {
            throw new EIException(ErrorConfig.MENU_NAME_DUPLICATE_CODE, ErrorConfig.MENU_NAME_DUPLICATE_MSG);
        }
    }

    private void validateMenuTypeFields(SysMenu menu) {
        if (menu.getMenuType() == null) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "菜单类型不能为空");
        }
        if (menu.getMenuType() == MENU_TYPE_PAGE && StringUtils.isBlank(menu.getComponentPath())) {
            throw new EIException(ErrorConfig.MENU_COMPONENT_REQUIRED_CODE, ErrorConfig.MENU_COMPONENT_REQUIRED_MSG);
        }
        if (menu.getMenuType() == MENU_TYPE_BUTTON) {
            menu.setComponentPath(null);
            menu.setRoutePath(null);
        }
    }

    /**
     * 若菜单被 role_level 低于当前用户（权限更高）的角色绑定，则拒绝操作。
     */
    private void assertMenuNotBoundByHigherRole(Long menuId) {
        List<SysRoleMenu> bindings = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getMenuId, menuId));
        if (bindings.isEmpty()) {
            return;
        }

        List<Long> roleIds = bindings.stream().map(SysRoleMenu::getRoleId).distinct().collect(Collectors.toList());
        List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
        if (roles == null) {
            roles = Collections.emptyList();
        }

        int currentLevel = SecurityUtils.getCurrentLevel();
        for (SysRole role : roles) {
            if (role.getRoleLevel() != null && role.getRoleLevel() < currentLevel) {
                log.warn("用户等级 {} 尝试操作被等级 {} 角色绑定的菜单 id={}", currentLevel, role.getRoleLevel(), menuId);
                throw new EIException(ErrorConfig.MENU_BOUND_HIGHER_ROLE_CODE, ErrorConfig.MENU_BOUND_HIGHER_ROLE_MSG);
            }
        }
    }
}
