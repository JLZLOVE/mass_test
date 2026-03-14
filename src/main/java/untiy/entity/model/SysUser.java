package untiy.entity.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户基础表
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 登录账号（学号/工号）
     */
    private String username;

    /**
     * 加密密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 性别 0:未知 1:男 2:女
     */
    private Integer gender;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

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
     * 身份证号（敏感，仅少数人有权限查看）
     */
    private String idCard;

    /**
     * 状态 0:禁用 1:正常
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
