package untiy.utils;

import org.junit.jupiter.api.Test;
import untiy.exception.EIException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateCodeUtilTest {

    @Test
    void generate_matchesPattern() {
        LocalDateTime time = LocalDateTime.of(2026, 7, 16, 14, 30, 45);
        String code = TemplateCodeUtil.generate("CLUB", time);
        assertTrue(code.startsWith("CLUB_202607161430_"));
        assertEquals(24, code.length());
    }

    @Test
    void assertMatchesCreateTime_passesWhenMinuteEqual() {
        LocalDateTime createTime = LocalDateTime.of(2026, 7, 16, 14, 30, 45);
        String code = TemplateCodeUtil.generate("CLUB", createTime);
        TemplateCodeUtil.assertMatchesCreateTime(code, createTime);
    }

    @Test
    void assertMatchesCreateTime_failsWhenTampered() {
        LocalDateTime createTime = LocalDateTime.of(2026, 7, 16, 14, 30, 45);
        String code = "CLUB_202607161431_827391";
        assertThrows(EIException.class, () -> TemplateCodeUtil.assertMatchesCreateTime(code, createTime));
    }
}
