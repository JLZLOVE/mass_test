package untiy.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import untiy.entity.BaseQuery;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.utils.SecurityUtils;

/**
 * 全局通用等级访问控制：基于「数值越小权限越高」约定，供各业务模块复用。
 * <p>
 * 等级字段语义与 {@link untiy.exception.Level}、{@code sys_role.role_level} 一致。
 */
public final class LevelBasedAccess {

    private LevelBasedAccess() {
    }

    /**
     * 校验当前用户是否有权<strong>查看</strong>目标等级资源。
     * <p>
     * 目标等级数值小于当前用户等级（更高权限）时拒绝访问。
     *
     * @param currentLevel 当前用户有效等级
     * @param targetLevel  目标资源等级
     */
    public static void checkViewable(int currentLevel, int targetLevel) {
        if (targetLevel < currentLevel) {
            throw new EIException(ErrorConfig.ROLE_NO_PERMISSION_CODE, ErrorConfig.ROLE_NO_PERMISSION_MSG);
        }
    }

    /**
     * 校验当前用户是否有权<strong>操作</strong>（修改/删除）目标等级资源。
     * <p>
     * 规则同 {@link #checkViewable}：不可操作高于自身权限的资源。
     *
     * @param currentLevel 当前用户有效等级
     * @param targetLevel  目标资源等级
     */
    public static void checkOperable(int currentLevel, int targetLevel) {
        if (targetLevel < currentLevel) {
            throw new EIException(ErrorConfig.ROLE_CANNOT_MODIFY_HIGHER_CODE, ErrorConfig.ROLE_CANNOT_MODIFY_HIGHER_MSG);
        }
    }

    /**
     * 校验当前用户是否有权将资源设置为指定新等级（新增或变更等级字段）。
     * <p>
     * 新等级数值小于当前用户等级（试图创建/提升为更高权限）时拒绝。
     *
     * @param currentLevel 当前用户有效等级
     * @param newLevel     拟设置的新等级
     */
    public static void checkNewLevel(int currentLevel, int newLevel) {
        if (newLevel < currentLevel) {
            throw new EIException(ErrorConfig.ROLE_CANNOT_ELEVATE_CODE, ErrorConfig.ROLE_CANNOT_ELEVATE_MSG);
        }
    }

    /**
     * 向查询条件叠加等级过滤：仅返回等级数值 ≥ {@code currentLevel} 的记录（不高于当前用户权限）。
     *
     * @param wrapper      MyBatis-Plus 查询包装器
     * @param currentLevel 当前用户有效等级
     * @param levelColumn  等级字段列名（数据库下划线命名，如 {@code role_level}）
     */
    public static <T> void applyLevelFilter(QueryWrapper<T> wrapper, int currentLevel, String levelColumn) {
        if (wrapper == null || levelColumn == null || levelColumn.isEmpty()) {
            return;
        }
        wrapper.ge(levelColumn, currentLevel);
    }

    /**
     * 向 {@link BaseQuery} 注入当前用户的数据范围字段（委托 {@link DataScopeHelper}）。
     *
     * @param query        查询参数对象
     * @param currentLevel 当前用户有效等级，传 null 时从 {@link SecurityUtils} 读取
     */
    public static void applyLevelFilter(BaseQuery query, Integer currentLevel) {
        if (query == null) {
            return;
        }
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        int level = currentLevel != null ? currentLevel : user.getEffectiveLevel();
        DataScopeHelper.applyLevelScope(query, level, user);
    }
}
