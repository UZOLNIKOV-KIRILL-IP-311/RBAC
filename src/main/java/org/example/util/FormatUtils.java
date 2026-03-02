package org.example.util;

import java.util.List;

/**
 * Утилиты для форматированного вывода данных.
 */
public class FormatUtils {

    private static final char BORDER_CHAR = '-';
    private static final char CORNER_CHAR = '+';
    private static final char SEPARATOR_CHAR = '|';

    private FormatUtils() {
        // Utility class
    }

    /**
     * Форматирует данные в виде ASCII-таблицы с рамками.
     *
     * @param headers заголовки столбцов
     * @param rows строки данных
     * @return отформатированная таблица
     */
    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            throw new IllegalArgumentException("Headers cannot be empty");
        }

        int columnCount = headers.length;
        int[] columnWidths = new int[columnCount];

        // Инициализируем ширины столбцов заголовками
        for (int i = 0; i < columnCount; i++) {
            columnWidths[i] = headers[i] != null ? headers[i].length() : 0;
        }

        // Находим максимальную ширину для каждого столбца
        for (String[] row : rows) {
            for (int i = 0; i < columnCount; i++) {
                String cell = (i < row.length && row[i] != null) ? row[i] : "";
                columnWidths[i] = Math.max(columnWidths[i], cell.length());
            }
        }

        StringBuilder sb = new StringBuilder();

        // Верхняя граница
        sb.append(formatHorizontalBorder(columnWidths)).append("\n");

        // Заголовки
        sb.append(SEPARATOR_CHAR);
        for (int i = 0; i < columnCount; i++) {
            sb.append(" ").append(padRight(headers[i], columnWidths[i])).append(" ").append(SEPARATOR_CHAR);
        }
        sb.append("\n");

        // Разделитель после заголовков
        sb.append(formatHorizontalBorder(columnWidths)).append("\n");

        // Строки данных
        for (String[] row : rows) {
            sb.append(SEPARATOR_CHAR);
            for (int i = 0; i < columnCount; i++) {
                String cell = (i < row.length && row[i] != null) ? row[i] : "";
                sb.append(" ").append(padRight(cell, columnWidths[i])).append(" ").append(SEPARATOR_CHAR);
            }
            sb.append("\n");
        }

        // Нижняя граница
        sb.append(formatHorizontalBorder(columnWidths));

        return sb.toString();
    }

    /**
     * Форматирует заголовок для таблицы с нумерацией строк.
     *
     * @param headers заголовки столбцов
     * @param rows строки данных
     * @param includeLineNumber включить нумерацию строк
     * @return отформатированная таблица
     */
    public static String formatTableWithLineNumbers(String[] headers, List<String[]> rows, boolean includeLineNumber) {
        if (!includeLineNumber) {
            return formatTable(headers, rows);
        }

        // Добавляем столбец для номера строки
        String[] newHeaders = new String[headers.length + 1];
        newHeaders[0] = "#";
        System.arraycopy(headers, 0, newHeaders, 1, headers.length);

        String[][] newRows = new String[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            newRows[i] = new String[headers.length + 1];
            newRows[i][0] = String.valueOf(i + 1);
            System.arraycopy(rows.get(i), 0, newRows[i], 1, rows.get(i).length);
        }

        return formatTable(newHeaders, List.of(newRows));
    }

    /**
     * Создаёт горизонтальную границу таблицы.
     *
     * @param columnWidths ширины столбцов
     * @return строка границы
     */
    private static String formatHorizontalBorder(int[] columnWidths) {
        StringBuilder sb = new StringBuilder();
        sb.append(CORNER_CHAR);

        for (int width : columnWidths) {
            sb.append(repeatChar(BORDER_CHAR, width + 2));
            sb.append(CORNER_CHAR);
        }

        return sb.toString();
    }

    /**
     * Обрамление текста рамкой.
     *
     * @param text текст для обрамления
     * @return текст в рамке
     */
    public static String formatBox(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String[] lines = text.split("\n");
        int maxWidth = 0;

        for (String line : lines) {
            maxWidth = Math.max(maxWidth, line.length());
        }

        StringBuilder sb = new StringBuilder();

        // Верхняя граница
        sb.append(CORNER_CHAR).append(repeatChar(BORDER_CHAR, maxWidth + 2)).append(CORNER_CHAR).append("\n");

        // Строки текста
        for (String line : lines) {
            sb.append(SEPARATOR_CHAR).append(" ").append(padRight(line, maxWidth)).append(" ").append(SEPARATOR_CHAR).append("\n");
        }

        // Нижняя граница
        sb.append(CORNER_CHAR).append(repeatChar(BORDER_CHAR, maxWidth + 2)).append(CORNER_CHAR);

        return sb.toString();
    }

    /**
     * Форматирование заголовка секции.
     *
     * @param text заголовок
     * @return отформатированный заголовок
     */
    public static String formatHeader(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        int totalWidth = Math.max(text.length() + 4, 60);
        int padding = (totalWidth - text.length()) / 2;

        StringBuilder sb = new StringBuilder();

        sb.append(repeatChar('=', totalWidth)).append("\n");
        sb.append(repeatChar(' ', padding)).append(text).append("\n");
        sb.append(repeatChar('=', totalWidth));

        return sb.toString();
    }

    /**
     * Форматирование заголовка секции с подзаголовком.
     *
     * @param title заголовок
     * @param subtitle подзаголовок
     * @return отформатированный заголовок
     */
    public static String formatHeader(String title, String subtitle) {
        StringBuilder sb = new StringBuilder();

        sb.append(formatHeader(title)).append("\n");

        if (subtitle != null && !subtitle.isEmpty()) {
            sb.append(formatSubheader(subtitle));
        }

        return sb.toString();
    }

    /**
     * Форматирование подзаголовка.
     *
     * @param text подзаголовок
     * @return отформатированный подзаголовок
     */
    public static String formatSubheader(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return "\n--- " + text + " ---\n";
    }

    /**
     * Обрезка длинной строки с добавлением "...".
     *
     * @param text строка
     * @param maxLength максимальная длина
     * @return обрезанная строка
     */
    public static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        if (maxLength <= 3) {
            return text.substring(0, maxLength);
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Дополнение строки пробелами справа до указанной длины.
     *
     * @param text строка
     * @param length целевая длина
     * @return дополненная строка
     */
    public static String padRight(String text, int length) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= length) {
            return text.substring(0, length);
        }

        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < length) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Дополнение строки пробелами слева до указанной длины.
     *
     * @param text строка
     * @param length целевая длина
     * @return дополненная строка
     */
    public static String padLeft(String text, int length) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= length) {
            return text.substring(0, length);
        }

        StringBuilder sb = new StringBuilder();
        while (sb.length() + text.length() < length) {
            sb.append(' ');
        }
        sb.append(text);
        return sb.toString();
    }

    /**
     * Дополнение строки символом до указанной длины.
     *
     * @param text строка
     * @param length целевая длина
     * @param padChar символ для дополнения
     * @param left если true, дополнять слева
     * @return дополненная строка
     */
    public static String pad(String text, int length, char padChar, boolean left) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= length) {
            return text.substring(0, length);
        }

        StringBuilder sb = new StringBuilder();
        String padding = repeatChar(padChar, length - text.length());

        if (left) {
            sb.append(padding).append(text);
        } else {
            sb.append(text).append(padding);
        }

        return sb.toString();
    }

    /**
     * Повторить символ указанное количество раз.
     *
     * @param ch символ
     * @param count количество повторений
     * @return строка из повторённых символов
     */
    public static String repeatChar(char ch, int count) {
        if (count <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * Центрировать текст в указанной ширине.
     *
     * @param text текст
     * @param width ширина
     * @return центрированный текст
     */
    public static String center(String text, int width) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= width) {
            return text.substring(0, width);
        }

        int totalPadding = width - text.length();
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leftPadding; i++) {
            sb.append(' ');
        }
        sb.append(text);
        for (int i = 0; i < rightPadding; i++) {
            sb.append(' ');
        }

        return sb.toString();
    }

    /**
     * Создать разделительную линию.
     *
     * @param width ширина линии
     * @param ch символ для линии
     * @return строка-разделитель
     */
    public static String createLine(int width, char ch) {
        return repeatChar(ch, width);
    }

    /**
     * Отформатировать список элементов с маркерами.
     *
     * @param items список элементов
     * @param marker символ маркера
     * @return отформатированный список
     */
    public static String formatList(List<String> items, char marker) {
        if (items == null || items.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append("  ").append(marker).append(" ").append(item).append("\n");
        }

        // Удаляем последний символ новой строки
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    /**
     * Отформатировать список элементов с нумерацией.
     *
     * @param items список элементов
     * @return отформатированный нумерованный список
     */
    public static String formatNumberedList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append("  ").append(i + 1).append(". ").append(items.get(i)).append("\n");
        }

        // Удаляем последний символ новой строки
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }
}
