package project.shared.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestValidatorTest {

    @Test
    void validRequestPasses() {
        assertTrue(RequestValidator.isValidRequest("SEARCH_RISTORANTI:Milano::20::"));
    }

    @Test
    void invalidRequestWithNewLineFails() {
        assertFalse(RequestValidator.isValidRequest("LOGIN:test@example.com\npassword"));
    }

    @Test
    void validEmailPasses() {
        assertTrue(RequestValidator.isValidEmail("user@example.com"));
    }

    @Test
    void invalidEmailFails() {
        assertFalse(RequestValidator.isValidEmail("not-an-email"));
    }

    @Test
    void validPasswordPasses() {
        assertTrue(RequestValidator.isValidPassword("Secure1Pass"));
    }

    @Test
    void invalidPasswordFailsWhenRequirementsAreMissing() {
        assertFalse(RequestValidator.isValidPassword("weak"));
        assertFalse(RequestValidator.isValidPassword("lowercase1"));
        assertFalse(RequestValidator.isValidPassword("UPPERCASE1"));
        assertFalse(RequestValidator.isValidPassword("NoDigitsHere"));
    }

    @Test
    void validCoordinatePasses() {
        assertTrue(RequestValidator.isValidCoordinate(45.4642, 9.19));
    }

    @Test
    void invalidCoordinateFails() {
        assertFalse(RequestValidator.isValidCoordinate(91, 0));
    }
}
