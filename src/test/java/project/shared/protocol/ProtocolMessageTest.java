package project.shared.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolMessageTest {

    @Test
    void buildsMessageWithParts() {
        String message = ProtocolMessage.build("SEARCH_RISTORANTI", "Milano", "Italiana", 20, 50);
        assertEquals("SEARCH_RISTORANTI:Milano:Italiana:20:50", message);
    }

    @Test
    void attachesSessionTokenOnce() {
        String request = ProtocolMessage.attachSessionToken("GET_USER_PROFILE:12", "abc123");
        assertTrue(request.endsWith(":TOKEN:abc123"));
    }

    @Test
    void safeMessagesAreAccepted() {
        assertTrue(ProtocolMessage.isSafe("PING"));
    }

    @Test
    void unsafeMessagesAreRejected() {
        assertFalse(ProtocolMessage.isSafe("LOGIN:bad\nuser"));
    }
}
