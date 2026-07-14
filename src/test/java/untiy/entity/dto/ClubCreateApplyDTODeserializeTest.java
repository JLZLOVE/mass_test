package untiy.entity.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClubCreateApplyDTODeserializeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserializeProposedLeaderUsernameAsString() throws Exception {
        String json = """
                {
                  "clubName": "学生会",
                  "collegeId": 5,
                  "category": "自律互助类",
                  "description": "测试",
                  "proposedLeaderUsername": "202320164602",
                  "maxMembers": 100
                }
                """;
        ClubCreateApplyDTO dto = mapper.readValue(json, ClubCreateApplyDTO.class);
        assertEquals("202320164602", dto.getProposedLeaderUsername());
    }

    @Test
    void deserializeProposedLeaderUsernameAsNumber() throws Exception {
        String json = """
                {
                  "clubName": "学生会",
                  "collegeId": 5,
                  "category": "自律互助类",
                  "proposedLeaderUsername": 202320164602,
                  "maxMembers": 100
                }
                """;
        ClubCreateApplyDTO dto = mapper.readValue(json, ClubCreateApplyDTO.class);
        assertEquals("202320164602", dto.getProposedLeaderUsername());
    }

    @Test
    void missingProposedLeaderUsernameIsNull() throws Exception {
        String json = """
                {
                  "clubName": "学生会",
                  "collegeId": 5,
                  "category": "自律互助类",
                  "maxMembers": 100
                }
                """;
        ClubCreateApplyDTO dto = mapper.readValue(json, ClubCreateApplyDTO.class);
        assertNull(dto.getProposedLeaderUsername());
    }
}
