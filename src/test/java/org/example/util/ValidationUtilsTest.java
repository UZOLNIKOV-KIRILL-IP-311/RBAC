package org.example.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void testIsValidUsername() {
        // Valid usernames
        assertTrue(ValidationUtils.isValidUsername("abc"));
        assertTrue(ValidationUtils.isValidUsername("user_123"));
        assertTrue(ValidationUtils.isValidUsername("test_user_name"));
        assertTrue(ValidationUtils.isValidUsername("validuser"));
        assertTrue(ValidationUtils.isValidUsername("a".repeat(20)));

        // Invalid usernames
        assertFalse(ValidationUtils.isValidUsername(null));
        assertFalse(ValidationUtils.isValidUsername(""));
        assertFalse(ValidationUtils.isValidUsername("ab")); // too short
        assertFalse(ValidationUtils.isValidUsername("a".repeat(21))); // too long
        assertFalse(ValidationUtils.isValidUsername("user name")); // contains space
        assertFalse(ValidationUtils.isValidUsername("user@name")); // contains @
        assertFalse(ValidationUtils.isValidUsername("user-name")); // contains -
        assertFalse(ValidationUtils.isValidUsername("USER")); // uppercase only (actually valid)
    }

    @Test
    void testIsValidEmail() {
        // Valid emails
        assertTrue(ValidationUtils.isValidEmail("test@example.com"));
        assertTrue(ValidationUtils.isValidEmail("user.name@domain.org"));
        assertTrue(ValidationUtils.isValidEmail("user+tag@example.co.uk"));

        // Invalid emails
        assertFalse(ValidationUtils.isValidEmail(null));
        assertFalse(ValidationUtils.isValidEmail(""));
        assertFalse(ValidationUtils.isValidEmail("invalid-email")); // no @
        assertFalse(ValidationUtils.isValidEmail("@example.com")); // no local part
        assertFalse(ValidationUtils.isValidEmail("user@")); // no domain
        assertFalse(ValidationUtils.isValidEmail("user@example")); // no TLD
        assertFalse(ValidationUtils.isValidEmail("user..name@example.com")); // double dots
    }

    @Test
    void testIsValidDate() {
        // Valid dates
        assertTrue(ValidationUtils.isValidDate("2024-01-15"));
        assertTrue(ValidationUtils.isValidDate("2026-12-31"));

        // Invalid dates
        assertFalse(ValidationUtils.isValidDate(null));
        assertFalse(ValidationUtils.isValidDate(""));
        assertFalse(ValidationUtils.isValidDate("2024/01/15")); // wrong separator
        assertFalse(ValidationUtils.isValidDate("15-01-2024")); // wrong order
        assertFalse(ValidationUtils.isValidDate("2024-1-15")); // single digit month
        assertFalse(ValidationUtils.isValidDate("2024-01-5")); // single digit day
    }

    @Test
    void testIsValidDateTime() {
        // Valid date-times
        assertTrue(ValidationUtils.isValidDateTime("2024-01-15 10:30"));
        assertTrue(ValidationUtils.isValidDateTime("2026-12-31 23:59"));

        // Invalid date-times
        assertFalse(ValidationUtils.isValidDateTime(null));
        assertFalse(ValidationUtils.isValidDateTime(""));
        assertFalse(ValidationUtils.isValidDateTime("2024-01-15")); // date only
        assertFalse(ValidationUtils.isValidDateTime("2024-01-15 10:30:00")); // with seconds
        assertFalse(ValidationUtils.isValidDateTime("2024-01-15 10:30 AM")); // with AM/PM
    }

    @Test
    void testNormalizeString() {
        // Basic normalization
        assertEquals("hello world", ValidationUtils.normalizeString("  hello   world  "));
        assertEquals("test", ValidationUtils.normalizeString("  test  "));

        // To lower case
        assertEquals("hello world", ValidationUtils.normalizeString("HELLO WORLD", true, false));

        // To upper case
        assertEquals("HELLO WORLD", ValidationUtils.normalizeString("hello world", false, true));

        // Null handling
        assertEquals("", ValidationUtils.normalizeString(null));
    }

    @Test
    void testRequireNonEmpty() {
        // Should not throw for valid values
        assertDoesNotThrow(() -> ValidationUtils.requireNonEmpty("valid", "field"));
        assertDoesNotThrow(() -> ValidationUtils.requireNonEmpty("  valid  ", "field"));

        // Should throw for null
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty(null, "field"));
        assertEquals("field cannot be empty", ex1.getMessage());

        // Should throw for empty
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty("", "field"));
        assertEquals("field cannot be empty", ex2.getMessage());

        // Should throw for whitespace only
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty("   ", "field"));
        assertEquals("field cannot be empty", ex3.getMessage());
    }

    @Test
    void testRequireNonNull() {
        // Should not throw for valid values
        assertDoesNotThrow(() -> ValidationUtils.requireNonNull("valid", "field"));
        assertDoesNotThrow(() -> ValidationUtils.requireNonNull(123, "field"));

        // Should throw for null
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonNull(null, "field"));
        assertEquals("field cannot be null", ex.getMessage());
    }

    @Test
    void testIsNonEmpty() {
        assertTrue(ValidationUtils.isNonEmpty("valid", "field"));
        assertTrue(ValidationUtils.isNonEmpty("  valid  ", "field"));

        assertFalse(ValidationUtils.isNonEmpty(null, "field"));
        assertFalse(ValidationUtils.isNonEmpty("", "field"));
        assertFalse(ValidationUtils.isNonEmpty("   ", "field"));
    }
}
