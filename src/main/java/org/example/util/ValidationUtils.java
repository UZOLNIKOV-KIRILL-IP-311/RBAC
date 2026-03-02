package org.example.util;

import java.util.regex.Pattern;

/**
 * Утилитный класс для валидации данных.
 */
public class ValidationUtils {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern EMAIL_CONTIGUOUS_DOTS_PATTERN = Pattern.compile(".*\\.\\..*");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern DATE_TIME_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$");

    private ValidationUtils() {
        // Utility class
    }

    /**
     * Проверяет корректность имени пользователя.
     * Имя должно содержать только латинские буквы, цифры и подчёркивания,
     * длина от 3 до 20 символов.
     *
     * @param username имя пользователя для проверки
     * @return true, если имя пользователя корректно
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Проверяет корректность email адреса.
     * Email должен содержать локальную часть, @ и домен с TLD.
     * Не допускаются смежные точки.
     *
     * @param email email для проверки
     * @return true, если email корректен
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String trimmedEmail = email.trim();
        if (EMAIL_CONTIGUOUS_DOTS_PATTERN.matcher(trimmedEmail).matches()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(trimmedEmail).matches();
    }

    /**
     * Проверяет формат даты в формате YYYY-MM-DD.
     *
     * @param date строка даты для проверки
     * @return true, если формат даты корректен
     */
    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        return DATE_PATTERN.matcher(date.trim()).matches();
    }

    /**
     * Проверяет формат даты и времени в формате YYYY-MM-DD HH:MM.
     *
     * @param dateTime строка даты и времени для проверки
     * @return true, если формат корректен
     */
    public static boolean isValidDateTime(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) {
            return false;
        }
        return DATE_TIME_PATTERN.matcher(dateTime.trim()).matches();
    }

    /**
     * Нормализует строку: удаляет лишние пробелы и приводит к нужному регистру.
     *
     * @param input строка для нормализации
     * @param toLowerCase если true, приводит к нижнему регистру
     * @param toUpperCase если true, приводит к верхнему регистру
     * @return нормализованная строка
     */
    public static String normalizeString(String input, boolean toLowerCase, boolean toUpperCase) {
        if (input == null) {
            return "";
        }
        String normalized = input.trim().replaceAll("\\s+", " ");
        if (toLowerCase && !toUpperCase) {
            return normalized.toLowerCase();
        } else if (toUpperCase && !toLowerCase) {
            return normalized.toUpperCase();
        }
        return normalized;
    }

    /**
     * Нормализует строку: удаляет лишние пробелы.
     *
     * @param input строка для нормализации
     * @return нормализованная строка
     */
    public static String normalizeString(String input) {
        return normalizeString(input, false, false);
    }

    /**
     * Проверяет, что строка не пустая.
     *
     * @param value строка для проверки
     * @param fieldName имя поля для сообщения об ошибке
     * @throws IllegalArgumentException если строка пустая или null
     */
    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    /**
     * Проверяет, что объект не null.
     *
     * @param obj объект для проверки
     * @param fieldName имя поля для сообщения об ошибке
     * @throws IllegalArgumentException если объект null
     */
    public static void requireNonNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    /**
     * Проверяет, что строка не null и не пустая.
     *
     * @param value строка для проверки
     * @param fieldName имя поля для сообщения об ошибке
     * @return true, если строка не пустая
     */
    public static boolean isNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return true;
    }
}
