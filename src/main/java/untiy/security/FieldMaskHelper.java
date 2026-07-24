package untiy.security;

import untiy.entity.dto.SysUserDTO;
import untiy.exception.Level;

/**
 * DTO 字段脱敏：仅根据当前登录用户权限等级控制返回字段显隐，与准入鉴权、数据范围过滤解耦。
 */
public final class FieldMaskHelper {

    private FieldMaskHelper() {
    }

    /**
     * 按查看者权限等级脱敏用户 DTO。
     * <ul>
     *   <li>等级 0~1 或查看本人：除密码外完整返回</li>
     *   <li>等级 2（社长）：隐藏联系方式明文</li>
     *   <li>等级 3（部长）：额外隐藏学号/工号</li>
     *   <li>等级 4 查看他人：仅保留基础公开字段</li>
     * </ul>
     */
    public static void maskSysUserDto(SysUserDTO dto, LoginUserDetails viewer) {
        if (dto == null) {
            return;
        }
        dto.setPassword(null);

        if (viewer == null) {
            clearSensitiveFields(dto);
            return;
        }

        boolean isSelf = dto.getId() != null && dto.getId().equals(viewer.getUserId());
        int level = viewer.getEffectiveLevel();

        if (level <= Level.ADMIN || isSelf) {
            return;
        }
        // Level 2+：创建时间仅管理人员可见
        dto.setCreateTime(null);
        if (level == Level.CLUB_LEADER) {
            maskContactFields(dto);
            return;
        }
        if (level == Level.DEPT_LEADER) {
            dto.setStudentNo(null);
            dto.setTeacherNo(null);
            maskContactFields(dto);
            return;
        }
        if (!isSelf) {
            clearSensitiveFields(dto);
        }
    }

    private static void clearSensitiveFields(SysUserDTO dto) {
        dto.setPhone(null);
        dto.setEmail(null);
        dto.setStudentNo(null);
        dto.setTeacherNo(null);
        dto.setCreateTime(null);
    }

    private static void maskContactFields(SysUserDTO dto) {
        if (dto.getPhone() != null && dto.getPhone().length() >= 7) {
            String phone = dto.getPhone();
            dto.setPhone(phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4));
        }
        if (dto.getEmail() != null && dto.getEmail().contains("@")) {
            int at = dto.getEmail().indexOf('@');
            dto.setEmail(dto.getEmail().charAt(0) + "***" + dto.getEmail().substring(at));
        }
    }
}
