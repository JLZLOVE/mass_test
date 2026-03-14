package untiy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_user")
public class RegisterDTO implements Serializable {
    /**
     * 学号/工号）
     */
    private String username;

    private final static int GENDER_TYPE=0;
    @NotBlank(message = "姓名不能为空")
    private String realName;          // 真实姓名

    @NotBlank(message = "密码不能为空")
    private String password;           // 明文密码

    private Integer gender=GENDER_TYPE;        // 性别，默认未知

    private Integer userType ;
}
