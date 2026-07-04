package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import untiy.entity.SysMenu;
import untiy.entity.SysRole;
import untiy.entity.SysRoleMenu;
import untiy.entity.dto.AssignRoleMenuDTO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.mapper.SysMenuMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysRoleMenuMapper;
import untiy.security.LevelBasedAccess;
import untiy.security.MenuCacheHelper;
import untiy.service.SysRoleMenuService;
import untiy.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Transactional
    @Override
    public void assign(AssignRoleMenuDTO dto) {
        if (dto == null || dto.getRoleId() == null) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, ErrorConfig.BAD_REQUEST_MSG);
        }

        loadRoleAndCheckOperable(dto.getRoleId());
        List<Long> menuIds = normalizeMenuIds(dto.getMenuIds());
        assertAllMenusExist(menuIds);

        remove(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, dto.getRoleId()));

        if (!menuIds.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            List<SysRoleMenu> batch = new ArrayList<>(menuIds.size());
            for (Long menuId : menuIds) {
                SysRoleMenu entity = new SysRoleMenu();
                entity.setRoleId(dto.getRoleId());
                entity.setMenuId(menuId);
                entity.setCreateTime(now);
                batch.add(entity);
            }
            saveBatch(batch);
        }

        MenuCacheHelper.evictAll(redisTemplate);
        log.info("角色 id={} 菜单全量分配完成，绑定 {} 个菜单", dto.getRoleId(), menuIds.size());
    }

    @Override
    public List<Long> listMenuIdsByRole(Long roleId) {
        loadRoleAndCheckOperable(roleId);

        return list(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId)
                .select(SysRoleMenu::getMenuId))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private SysRole loadRoleAndCheckOperable(Long roleId) {
        if (roleId == null) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, ErrorConfig.BAD_REQUEST_MSG);
        }
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new EIException(ErrorConfig.ROLE_NOT_FOUND_CODE, ErrorConfig.ROLE_NOT_FOUND_MSG);
        }
        int currentLevel = SecurityUtils.getCurrentLevel();
        if (role.getRoleLevel() != null && role.getRoleLevel() < currentLevel) {
            log.warn("用户等级 {} 尝试操作等级 {} 的角色 id={} 的菜单（越权）", currentLevel, role.getRoleLevel(), roleId);
            LevelBasedAccess.checkOperable(currentLevel, role.getRoleLevel());
        }
        return role;
    }

    private List<Long> normalizeMenuIds(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return Collections.emptyList();
        }
        return menuIds.stream()
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
    }

    private void assertAllMenusExist(List<Long> menuIds) {
        if (menuIds.isEmpty()) {
            return;
        }
        long count = sysMenuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getStatus, 1));
        if (count != menuIds.size()) {
            throw new EIException(ErrorConfig.MENU_IDS_INVALID_CODE, ErrorConfig.MENU_IDS_INVALID_MSG);
        }
    }
}
