package untiy.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户新增/编辑入参 / 列表出参（脱敏后）
 */
@Data
@Schema(name = "SysUserDTO", description = "用户新增、修改入参对象")
public class SysUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户主键ID，新增不用传，编辑时传入 */
    private Long id;

    /** 登录账号（学号/工号） */
    private String username;

    /** 加密密码，前端明文提交 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 性别 0:未知 1:男 2:女 */
    private Integer gender;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;

    /** 用户类型 1:学生 2:老师 3:管理员 */
    private Integer userType;

    /** 学号（仅学生） */
    private String studentNo;

    /** 工号（仅老师） */
    private String teacherNo;

    /** 状态 0:禁用 1:正常 */
    private Integer status;

    /** 创建时间（列表展示；等级 >1 时由 FieldMaskHelper 清空） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
