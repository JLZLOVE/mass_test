package untiy.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import untiy.security.LoginUserDetails.CacheSnapshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoginUserDetailsCacheSnapshotTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void cacheSnapshot_roundTripViaJson() throws Exception {
        CacheSnapshot original = new CacheSnapshot(
                1001L, "202320164602", "encoded-pwd", 1, 3, 5L, 10L);

        String json = objectMapper.writeValueAsString(original);
        CacheSnapshot restored = objectMapper.readValue(json, CacheSnapshot.class);

        assertNotNull(restored);
        assertEquals(original.getUserId(), restored.getUserId());
        assertEquals(original.getUsername(), restored.getUsername());
        assertEquals(original.getPassword(), restored.getPassword());
        assertEquals(original.getStatus(), restored.getStatus());
        assertEquals(original.getEffectiveLevel(), restored.getEffectiveLevel());
        assertEquals(original.getPrimaryClubId(), restored.getPrimaryClubId());
        assertEquals(original.getPrimaryDepartmentId(), restored.getPrimaryDepartmentId());
    }
}
