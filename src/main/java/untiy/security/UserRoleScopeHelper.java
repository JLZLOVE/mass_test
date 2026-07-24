package untiy.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import untiy.entity.SysClub;
import untiy.entity.SysCollege;
import untiy.entity.SysDepartment;
import untiy.entity.SysRole;
import untiy.entity.SysUserRole;
import untiy.entity.constants.ClubApplyConstants;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysDepartmentMapper;
import untiy.mapper.SysUserRoleMapper;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 用户-角色分配时的 scope 合法性及防重复校验。
 */
public final class UserRoleScopeHelper {

    /** 全部数据 */
    public static final int DATA_SCOPE_ALL = 0;
    /** 本学院 */
    public static final int DATA_SCOPE_COLLEGE = 1;
    /** 本社团 */
    public static final int DATA_SCOPE_CLUB = 2;
    /** 本部门 */
    public static final int DATA_SCOPE_DEPARTMENT = 3;
    /** 仅自己 */
    public static final int DATA_SCOPE_SELF = 4;

    private static final int SCOPE_TYPE_COLLEGE = 1;
    private static final int SCOPE_TYPE_CLUB = 2;
    private static final int SCOPE_TYPE_DEPARTMENT = 3;

    private UserRoleScopeHelper() {
    }

    /**
     * 解析分配校验用的 data_scope：优先 role_code 常量，其次角色名兼容，最后库字段。
     */
    public static Integer resolveDataScope(SysRole role) {
        if (role == null) {
            return null;
        }
        Integer byCode = mapDataScopeByRoleCode(role.getRoleCode());
        if (byCode != null) {
            return byCode;
        }
        Integer byName = mapDataScopeByRoleName(role.getRoleName());
        if (byName != null) {
            return byName;
        }
        if (role.getDataScope() != null) {
            return role.getDataScope();
        }
        return mapDataScopeByRoleLevel(role.getRoleLevel());
    }

    /**
     * 按 role_code（含 ADVISOR_*）映射默认数据范围。
     */
    public static Integer mapDataScopeByRoleCode(String roleCode) {
        if (StringUtils.isBlank(roleCode)) {
            return null;
        }
        String code = roleCode.trim().toUpperCase(Locale.ROOT);
        if (ClubApplyConstants.ROLE_SUPER_ADMIN.equals(code)) {
            return DATA_SCOPE_ALL;
        }
        if (ClubApplyConstants.ROLE_ADMIN.equals(code)) {
            return DATA_SCOPE_COLLEGE;
        }
        if (ClubApplyConstants.ROLE_ADVISOR.equals(code) || code.startsWith(ClubApplyConstants.ROLE_ADVISOR + "_")) {
            return DATA_SCOPE_CLUB;
        }
        if (ClubApplyConstants.ROLE_CLUB_PRESIDENT.equals(code)) {
            return DATA_SCOPE_CLUB;
        }
        if (ClubApplyConstants.ROLE_CLUB_MINISTER.equals(code)) {
            return DATA_SCOPE_DEPARTMENT;
        }
        if (ClubApplyConstants.ROLE_MEMBER.equals(code)) {
            return DATA_SCOPE_SELF;
        }
        return null;
    }

    /** 兼容库中自定义中文角色名（截图：普通学生成员 / 社团社长 / 通用指导老师） */
    public static Integer mapDataScopeByRoleName(String roleName) {
        if (StringUtils.isBlank(roleName)) {
            return null;
        }
        String name = roleName.trim();
        if (name.contains("超级管理")) {
            return DATA_SCOPE_ALL;
        }
        if (name.contains("学院管理") || name.contains("校级管理")) {
            return DATA_SCOPE_COLLEGE;
        }
        if (name.contains("指导") || name.contains("社长")) {
            return DATA_SCOPE_CLUB;
        }
        if (name.contains("部长")) {
            return DATA_SCOPE_DEPARTMENT;
        }
        if (name.contains("成员") || name.contains("学生")) {
            return DATA_SCOPE_SELF;
        }
        return null;
    }

    private static Integer mapDataScopeByRoleLevel(Integer roleLevel) {
        if (roleLevel == null) {
            return null;
        }
        switch (roleLevel) {
            case 0:
                return DATA_SCOPE_ALL;
            case 1:
                return DATA_SCOPE_COLLEGE;
            case 2:
                return DATA_SCOPE_CLUB;
            case 3:
                return DATA_SCOPE_DEPARTMENT;
            case 4:
                return DATA_SCOPE_SELF;
            default:
                return null;
        }
    }

    public static void validateScope(Integer dataScope, Integer scopeType, Long scopeId,
                                     SysCollegeMapper collegeMapper,
                                     SysClubMapper clubMapper,
                                     SysDepartmentMapper departmentMapper) {
        if (dataScope == null) {
            throw new EIException(ErrorConfig.ROLE_SCOPE_NOT_CONFIGURED_CODE, ErrorConfig.ROLE_SCOPE_NOT_CONFIGURED_MSG);
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
                    throw new EIException(ErrorConfig.ROLE_SCOPE_COLLEGE_NOT_FOUND_CODE, ErrorConfig.ROLE_SCOPE_COLLEGE_NOT_FOUND_MSG);
                }
                break;
            case DATA_SCOPE_CLUB:
                requireScope(SCOPE_TYPE_CLUB, scopeType, scopeId);
                if (clubMapper.selectCount(new LambdaQueryWrapper<SysClub>().eq(SysClub::getId, scopeId)) == 0) {
                    throw new EIException(ErrorConfig.ROLE_SCOPE_CLUB_NOT_FOUND_CODE, ErrorConfig.ROLE_SCOPE_CLUB_NOT_FOUND_MSG);
                }
                break;
            case DATA_SCOPE_DEPARTMENT:
                requireScope(SCOPE_TYPE_DEPARTMENT, scopeType, scopeId);
                if (departmentMapper.selectCount(new LambdaQueryWrapper<SysDepartment>().eq(SysDepartment::getId, scopeId)) == 0) {
                    throw new EIException(ErrorConfig.ROLE_SCOPE_DEPARTMENT_NOT_FOUND_CODE, ErrorConfig.ROLE_SCOPE_DEPARTMENT_NOT_FOUND_MSG);
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
                throw new EIException(ErrorConfig.ROLE_ASSIGN_GLOBAL_CONFLICT_CODE,
                        ErrorConfig.ROLE_ASSIGN_GLOBAL_CONFLICT_MSG);
            }
            if (newIsGlobal && existHasScope) {
                throw new EIException(ErrorConfig.ROLE_ASSIGN_SCOPE_CONFLICT_CODE,
                        ErrorConfig.ROLE_ASSIGN_SCOPE_CONFLICT_MSG);
            }
        }
    }
}
