package org.example.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void testGetCurrentDate() {
        String date = DateUtils.getCurrentDate();
        assertNotNull(date);
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void testGetCurrentDateTime() {
        String dateTime = DateUtils.getCurrentDateTime();
        assertNotNull(dateTime);
        assertTrue(dateTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testGetCurrentDateTimeMinute() {
        String dateTime = DateUtils.getCurrentDateTimeMinute();
        assertNotNull(dateTime);
        assertTrue(dateTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"));
    }

    @Test
    void testIsBefore() {
        assertTrue(DateUtils.isBefore("2024-01-01", "2024-12-31"));
        assertFalse(DateUtils.isBefore("2024-12-31", "2024-01-01"));
        assertFalse(DateUtils.isBefore("2024-01-01", "2024-01-01"));
        assertFalse(DateUtils.isBefore(null, "2024-01-01"));
        assertFalse(DateUtils.isBefore("2024-01-01", null));
    }

    @Test
    void testIsBeforeDateTime() {
        assertTrue(DateUtils.isBeforeDateTime("2024-01-01 10:00", "2024-01-01 12:00"));
        assertFalse(DateUtils.isBeforeDateTime("2024-01-01 12:00", "2024-01-01 10:00"));
        assertFalse(DateUtils.isBeforeDateTime(null, "2024-01-01 10:00"));
    }

    @Test
    void testIsAfter() {
        assertTrue(DateUtils.isAfter("2024-12-31", "2024-01-01"));
        assertFalse(DateUtils.isAfter("2024-01-01", "2024-12-31"));
        assertFalse(DateUtils.isAfter("2024-01-01", "2024-01-01"));
    }

    @Test
    void testIsAfterDateTime() {
        assertTrue(DateUtils.isAfterDateTime("2024-01-01 12:00", "2024-01-01 10:00"));
        assertFalse(DateUtils.isAfterDateTime("2024-01-01 10:00", "2024-01-01 12:00"));
    }

    @Test
    void testIsEqual() {
        assertTrue(DateUtils.isEqual("2024-01-01", "2024-01-01"));
        assertFalse(DateUtils.isEqual("2024-01-01", "2024-01-02"));
        assertFalse(DateUtils.isEqual(null, "2024-01-01"));
    }

    @Test
    void testAddDays() {
        assertEquals("2024-01-10", DateUtils.addDays("2024-01-01", 9));
        assertEquals("2023-12-25", DateUtils.addDays("2024-01-01", -7));
        assertEquals("2024-01-01", DateUtils.addDays("2024-01-01", 0));

        assertThrows(IllegalArgumentException.class, () -> DateUtils.addDays(null, 1));
        assertThrows(IllegalArgumentException.class, () -> DateUtils.addDays("invalid", 1));
    }

    @Test
    void testAddHours() {
        assertEquals("2024-01-01 15:00", DateUtils.addHours("2024-01-01 10:00", 5));
        assertEquals("2024-01-01 05:00", DateUtils.addHours("2024-01-01 10:00", -5));

        assertThrows(IllegalArgumentException.class, () -> DateUtils.addHours(null, 1));
    }

    @Test
    void testAddMinutes() {
        assertEquals("2024-01-01 10:30", DateUtils.addMinutes("2024-01-01 10:00", 30));
        assertEquals("2024-01-01 09:30", DateUtils.addMinutes("2024-01-01 10:00", -30));
    }

    @Test
    void testDaysBetween() {
        assertEquals(10, DateUtils.daysBetween("2024-01-01", "2024-01-11"));
        assertEquals(-10, DateUtils.daysBetween("2024-01-11", "2024-01-01"));
        assertEquals(0, DateUtils.daysBetween("2024-01-01", "2024-01-01"));
        assertEquals(0, DateUtils.daysBetween(null, "2024-01-01"));
    }

    @Test
    void testHoursBetween() {
        assertEquals(5, DateUtils.hoursBetween("2024-01-01 10:00", "2024-01-01 15:00"));
        assertEquals(-5, DateUtils.hoursBetween("2024-01-01 15:00", "2024-01-01 10:00"));
        assertEquals(0, DateUtils.hoursBetween(null, "2024-01-01 10:00"));
    }

    @Test
    void testFormatRelativeTime() {
        String today = DateUtils.getCurrentDate();
        assertEquals("Today", DateUtils.formatRelativeTime(today));

        String tomorrow = DateUtils.addDays(today, 1);
        assertEquals("Tomorrow", DateUtils.formatRelativeTime(tomorrow));

        String yesterday = DateUtils.addDays(today, -1);
        assertEquals("Yesterday", DateUtils.formatRelativeTime(yesterday));

        String future = DateUtils.addDays(today, 5);
        assertTrue(DateUtils.formatRelativeTime(future).contains("in"));

        String past = DateUtils.addDays(today, -5);
        assertTrue(DateUtils.formatRelativeTime(past).contains("ago"));

        assertEquals("Unknown", DateUtils.formatRelativeTime(null));
        assertEquals("invalid", DateUtils.formatRelativeTime("invalid"));
    }

    @Test
    void testFormatRelativeDateTime() {
        String now = DateUtils.getCurrentDateTimeMinute();
        assertTrue(DateUtils.formatRelativeDateTime(now).contains("now") ||
                   DateUtils.formatRelativeDateTime(now).contains("minute"));

        assertEquals("Unknown", DateUtils.formatRelativeDateTime(null));
    }

    @Test
    void testIsValidDate() {
        assertTrue(DateUtils.isValidDate("2024-01-15"));
        assertFalse(DateUtils.isValidDate(null));
        assertFalse(DateUtils.isValidDate(""));
        assertFalse(DateUtils.isValidDate("invalid"));
        assertFalse(DateUtils.isValidDate("2024/01/15"));
    }

    @Test
    void testIsValidDateTime() {
        assertTrue(DateUtils.isValidDateTime("2024-01-15 10:30"));
        assertFalse(DateUtils.isValidDateTime(null));
        assertFalse(DateUtils.isValidDateTime(""));
        assertFalse(DateUtils.isValidDateTime("invalid"));
    }

    @Test
    void testParseDate() {
        assertNotNull(DateUtils.parseDate("2024-01-15"));

        assertThrows(IllegalArgumentException.class, () -> DateUtils.parseDate(null));
        assertThrows(IllegalArgumentException.class, () -> DateUtils.parseDate("invalid"));
    }

    @Test
    void testParseDateTime() {
        assertNotNull(DateUtils.parseDateTime("2024-01-15 10:30"));

        assertThrows(IllegalArgumentException.class, () -> DateUtils.parseDateTime(null));
        assertThrows(IllegalArgumentException.class, () -> DateUtils.parseDateTime("invalid"));
    }

    @Test
    void testFormatDate() {
        assertEquals("01/15/2024", DateUtils.formatDate("2024-01-15", "MM/dd/yyyy"));
        assertEquals("15-01-2024", DateUtils.formatDate("2024-01-15", "dd-MM-yyyy"));

        assertThrows(IllegalArgumentException.class, () -> DateUtils.formatDate(null, "MM/dd/yyyy"));
    }

    @Test
    void testGetDayOfWeek() {
        // 2024-01-15 is a Monday
        assertEquals("MONDAY", DateUtils.getDayOfWeek("2024-01-15"));
        assertEquals("Unknown", DateUtils.getDayOfWeek(null));
        assertEquals("Unknown", DateUtils.getDayOfWeek("invalid"));
    }
}
