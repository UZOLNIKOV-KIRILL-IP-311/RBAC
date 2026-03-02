package org.example.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FormatUtilsTest {

    @Test
    void testFormatTable() {
        String[] headers = {"Name", "Age", "City"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"John", "30", "New York"});
        rows.add(new String[]{"Jane", "25", "London"});

        String table = FormatUtils.formatTable(headers, rows);

        assertTrue(table.contains("+"));
        assertTrue(table.contains("|"));
        assertTrue(table.contains("Name"));
        assertTrue(table.contains("John"));
        assertTrue(table.contains("Jane"));
    }

    @Test
    void testFormatTableEmptyRows() {
        String[] headers = {"Name", "Age"};
        List<String[]> rows = new ArrayList<>();

        String table = FormatUtils.formatTable(headers, rows);

        assertTrue(table.contains("Name"));
        assertTrue(table.contains("Age"));
    }

    @Test
    void testFormatTableNullHeaders() {
        String[] headers = {};
        List<String[]> rows = new ArrayList<>();

        assertThrows(IllegalArgumentException.class, () -> FormatUtils.formatTable(headers, rows));
    }

    @Test
    void testFormatBox() {
        String box = FormatUtils.formatBox("Hello World");

        assertTrue(box.contains("+"));
        assertTrue(box.contains("-"));
        assertTrue(box.contains("|"));
        assertTrue(box.contains("Hello World"));
    }

    @Test
    void testFormatBoxEmpty() {
        assertEquals("", FormatUtils.formatBox(""));
        assertEquals("", FormatUtils.formatBox(null));
    }

    @Test
    void testFormatBoxMultiline() {
        String box = FormatUtils.formatBox("Line 1\nLine 2\nLine 3");

        assertTrue(box.contains("Line 1"));
        assertTrue(box.contains("Line 2"));
        assertTrue(box.contains("Line 3"));
    }

    @Test
    void testFormatHeader() {
        String header = FormatUtils.formatHeader("Test Title");

        assertTrue(header.contains("="));
        assertTrue(header.contains("Test Title"));
    }

    @Test
    void testFormatHeaderEmpty() {
        assertEquals("", FormatUtils.formatHeader(""));
        assertEquals("", FormatUtils.formatHeader(null));
    }

    @Test
    void testTruncate() {
        assertEquals("Hello", FormatUtils.truncate("Hello", 10));
        assertEquals("He...", FormatUtils.truncate("Hello World", 5));
        assertEquals("", FormatUtils.truncate(null, 5));
        assertEquals("abc", FormatUtils.truncate("abc", 3));
        assertEquals("ab", FormatUtils.truncate("abc", 2));
    }

    @Test
    void testPadRight() {
        assertEquals("hello     ", FormatUtils.padRight("hello", 10));
        assertEquals("hello", FormatUtils.padRight("hello", 5));
        assertEquals("hel", FormatUtils.padRight("hello", 3));
        assertEquals("          ", FormatUtils.padRight(null, 10));
    }

    @Test
    void testPadLeft() {
        assertEquals("     hello", FormatUtils.padLeft("hello", 10));
        assertEquals("hello", FormatUtils.padLeft("hello", 5));
        assertEquals("hel", FormatUtils.padLeft("hello", 3));
        assertEquals("          ", FormatUtils.padLeft(null, 10));
    }

    @Test
    void testRepeatChar() {
        assertEquals("-----", FormatUtils.repeatChar('-', 5));
        assertEquals("", FormatUtils.repeatChar('-', 0));
        assertEquals("====", FormatUtils.repeatChar('=', 4));
    }

    @Test
    void testCenter() {
        assertEquals("  hello   ", FormatUtils.center("hello", 10));
        assertEquals("hello", FormatUtils.center("hello", 5));
        assertEquals("hel", FormatUtils.center("hello", 3));
    }

    @Test
    void testCreateLine() {
        assertEquals("-----", FormatUtils.createLine(5, '-'));
        assertEquals("=====", FormatUtils.createLine(5, '='));
        assertEquals("", FormatUtils.createLine(0, '-'));
    }

    @Test
    void testFormatList() {
        List<String> items = new ArrayList<>();
        items.add("Item 1");
        items.add("Item 2");
        items.add("Item 3");

        String list = FormatUtils.formatList(items, '-');

        assertTrue(list.contains("- Item 1"));
        assertTrue(list.contains("- Item 2"));
        assertTrue(list.contains("- Item 3"));
    }

    @Test
    void testFormatListEmpty() {
        assertEquals("", FormatUtils.formatList(new ArrayList<>(), '-'));
        assertEquals("", FormatUtils.formatList(null, '-'));
    }

    @Test
    void testFormatNumberedList() {
        List<String> items = new ArrayList<>();
        items.add("First");
        items.add("Second");
        items.add("Third");

        String list = FormatUtils.formatNumberedList(items);

        assertTrue(list.contains("1. First"));
        assertTrue(list.contains("2. Second"));
        assertTrue(list.contains("3. Third"));
    }

    @Test
    void testFormatNumberedListEmpty() {
        assertEquals("", FormatUtils.formatNumberedList(new ArrayList<>()));
        assertEquals("", FormatUtils.formatNumberedList(null));
    }

    @Test
    void testPad() {
        assertEquals("hello*****", FormatUtils.pad("hello", 10, '*', false));
        assertEquals("*****hello", FormatUtils.pad("hello", 10, '*', true));
        assertEquals("hello", FormatUtils.pad("hello", 5, '*', false));
    }
}
