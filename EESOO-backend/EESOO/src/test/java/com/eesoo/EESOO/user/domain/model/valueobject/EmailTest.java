package com.eesoo.EESOO.user.domain.model.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class EmailTest {

    @Test
    void testValidEmail() {
        Email email = Email.of("test@example.com");
        assertTrue(email.isPresent());
        assertEquals("test@example.com", email.getValue());
    }

    @Test
    void testInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> Email.of("invalid-email"));
    }

    @Test
    void testNullEmail() {
        Email email = Email.of(null);
        assertFalse(email.isPresent());
        assertNull(email.getValue());
    }

    @Test
    void testBlankEmail() {
        Email email = Email.of("   ");
        assertFalse(email.isPresent());
        assertNull(email.getValue());
    }

    @Test
    void testEmailEquality() {
        Email email1 = Email.of("test@example.com");
        Email email2 = Email.of("TEST@example.com");
        assertEquals(email1, email2);
    }

    @Test
    void testEmailToString() {
        Email email = Email.of("test@example.com");
        assertEquals("test@example.com", email.toString());

        Email noEmail = Email.of(null);
        assertEquals("(no email)", noEmail.toString());
    }

}