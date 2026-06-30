package untiy.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户基础表 视图对象
 *
 * @author 玖
 * @since 2026-06-29
 */
@Data
public class SysUserPublicVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private Long id;
    /**
     * 登录账号（学号/工号）
     */
    private String username;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 性别 0:未知 1:男 2:女
     */


    /**
     * 头像URL
     */
    private String avatar;
    /**
     * 用户类型 1:学生 2:老师 3:管理员
     */
    private Integer userType;
    /**
     * 学号（仅学生）
     */
    private String studentNo;
    /**
     * 工号（仅老师）
     */
    private String teacherNo;
    /**
     * 状态 0:禁用 1:正常
     */
    private Integer status;

}