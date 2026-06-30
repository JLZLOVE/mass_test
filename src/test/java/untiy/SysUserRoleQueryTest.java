package untiy;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import untiy.entity.SysUserRole;
import untiy.service.SysUserRoleService;
import untiy.utils.MPUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SysUserRoleQueryTest {

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Test
    void camelToUnderline_mapsUserId() {
        assertEquals("user_id", MPUtil.camelToUnderline("userId"));
        assertEquals("role_id", MPUtil.camelToUnderline("roleId"));
    }

    /** 验证按 userId 查询不再产生 BadSqlGrammarException（列名应为 user_id） */
    @Test
    void queryByUserIdUsesSnakeCaseColumn() {
        SysUserRole condition = new SysUserRole();
        condition.setUserId(5L);
        QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(wrapper, condition);
        List<SysUserRole> list = sysUserRoleService.list(wrapper);
        assertFalse(list.isEmpty(), "admin(user_id=5) 应存在角色关联记录");
    }
}
