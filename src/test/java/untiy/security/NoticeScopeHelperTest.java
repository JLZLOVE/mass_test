package untiy.security;

import org.junit.jupiter.api.Test;
import untiy.entity.constants.NoticeConstants;
import untiy.exception.EIException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NoticeScopeHelperTest {

    @Test
    void assertReceiverValuesValid_rejectsNonJsonForRoles() {
        assertThrows(EIException.class, () ->
                NoticeScopeHelper.assertReceiverValuesValid(NoticeConstants.RECEIVER_ROLES, "abc"));
    }

    @Test
    void assertReceiverValuesValid_rejectsEmptyArray() {
        assertThrows(EIException.class, () ->
                NoticeScopeHelper.assertReceiverValuesValid(NoticeConstants.RECEIVER_USERS, "[]"));
    }

    @Test
    void assertReceiverValuesValid_acceptsValidArray() {
        assertDoesNotThrow(() ->
                NoticeScopeHelper.assertReceiverValuesValid(NoticeConstants.RECEIVER_CLUBS, "[1,2,3]"));
    }

    @Test
    void assertReceiverValuesValid_skipsAllStudents() {
        assertDoesNotThrow(() ->
                NoticeScopeHelper.assertReceiverValuesValid(NoticeConstants.RECEIVER_ALL_STUDENTS, "ignored"));
    }
}
