package untiy.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import untiy.entity.SysClub;
import untiy.entity.SysCollege;
import untiy.entity.SysDepartment;
import untiy.entity.SysUserRole;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysDepartmentMapper;
import untiy.mapper.SysUserRoleMapper;

import java.util.List;
import java.util.Objects;

/**
 * 用户-角色分配时的 scope 合法性及防重复校验。
 */
public final class UserRoleScopeHelper {

    /** 全部数据 */
    private static final int DATA_SCOPE_ALL = 0;
    /** 本学院 */
    private static final int DATA_SCOPE_COLLEGE = 1;
    /** 本社团 */
    private static final int DATA_SCOPE_CLUB = 2;
    /** 本部门 */
    private static final int DATA_SCOPE_DEPARTMENT = 3;
    /** 仅自己 */
    private static final int DATA_SCOPE_SELF = 4;

    /** sys_user_role.scope_type：1 学院 */
    private static final int SCOPE_TYPE_COLLEGE = 1;
    /** sys_user_role.scope_type：2 社团 */
    private static final int SCOPE_TYPE_CLUB = 2;
    /** sys_user_role.scope_type：3 部门 */
    private static final int SCOPE_TYPE_DEPARTMENT = 3;

    private UserRoleScopeHelper() {
    }

    /**
     * 根据角色 {@code dataScope} 校验 scopeType/scopeId 组合是否合法，并校验 scope 实体存在。
     * <p>
     * dataScope：0 全部、1 本学院、2 本社团、3 本部门、4 仅自己
     */
    public static void validateScope(Integer dataScope, Integer scopeType, Long scopeId,
                                     SysCollegeMapper collegeMapper,
                                     SysClubMapper clubMapper,
                                     SysDepartmentMapper departmentMapper) {
        if (dataScope == null) {
            throw new EIException(ErrorConfig.ROLE_SCOPE_INVALID_CODE, "角色未配置数据范围");
        }

        switch (dataScope) {
            case DATA_SCOPE_ALL:
            case DATA_SCOPE_SELF:
                if (scopeType != null || scopeId != null) {
                    throw new EIException(ErrorConfig.ROLE_SCOPE_INVALID_CODE, ErrorConfig.ROLE_SCOPE_INVALID_MSG);
                }
                break;
            case DATA_SCOPE_COLLEGE:
                requireScope(SCOPE_TYPE_COLLEGE, scopeType, scopeId);
                if (collegeMapper.selectCount(new LambdaQueryWrapper<SysCollege>().eq(SysCollege::getId, scopeId)) == 0) {
                    throw new EIException(ErrorConfig.ROLE_SCOPE_INVALID_CODE, "学院不存在");
                }
                break;
            case DATA_SCOPE_CLUB:
                requireScope(SCOPE_TYPE_CLUB, scopeType, scopeId);
                if (clubMapper.selectCount(new LambdaQueryWrapper<SysClub>().eq(SysClub::getId, scopeId)) == 0) {
                    throw new EIException(ErrorConfig.ROLE_SCOPE_INVALID_CODE, "社团不存在");
                }
                break;
            case DATA_SCOPE_DEPARTMENT:
                requireScope(SCOPE_TYPE_DEPARTMENT, scopeType, scopeId);
                if (departmentMapper.selectCount(new LambdaQueryWrapper<SysDepartment>().eq(SysDepartment::getId, scopeId)) == 0) {
                    throw new EIException(ErrorConfig.ROLE_SCOPE_INVALID_CODE, "部门不存在");
                }
                break;
            default:
                throw new EIException(ErrorConfig.ROLE_SCOPE_INVALID_CODE, ErrorConfig.ROLE_SCOPE_INVALID_MSG);
        }
    }

    private static void requireScope(int expectedType, Integer scopeType, Long scopeId) {
        if (scopeType == null || scopeType != expectedType || scopeId == null) {
            throw new EIException(ErrorConfig.ROLE_SCOPE_INVALID_CODE, ErrorConfig.ROLE_SCOPE_INVALID_MSG);
        }
    }

    /**
     * 防重复分配：精确匹配、全局与特定范围互斥。
     */
    public static void assertNoDuplicateAssignment(SysUserRoleMapper mapper,
                                                   Long userId, Long roleId,
                                                   Integer scopeType, Long scopeId) {
        List<SysUserRole> existing = mapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, roleId));

        boolean newIsGlobal = scopeType == null && scopeId == null;
        boolean newHasScope = scopeType != null || scopeId != null;

        for (SysUserRole ur : existing) {
            if (Objects.equals(ur.getScopeType(), scopeType) && Objects.equals(ur.getScopeId(), scopeId)) {
                throw new EIException(ErrorConfig.ROLE_ASSIGN_DUPLICATE_CODE, ErrorConfig.ROLE_ASSIGN_DUPLICATE_MSG);
            }
            boolean existGlobal = ur.getScopeType() == null && ur.getScopeId() == null;
            boolean existHasScope = ur.getScopeType() != null || ur.getScopeId() != null;

            if (newHasScope && existGlobal) {
                throw new EIException(ErrorConfig.ROLE_ASSIGN_DUPLICATE_CODE,
                        "该用户已拥有此角色的全局权限，无法再分配特定范围");
            }
            if (newIsGlobal && existHasScope) {
                throw new EIException(ErrorConfig.ROLE_ASSIGN_DUPLICATE_CODE,
                        "该用户已拥有此角色的特定范围权限，无法再分配全局权限");
            }
        }
    }
}
