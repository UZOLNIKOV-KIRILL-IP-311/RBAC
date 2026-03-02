package org.example.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Утилиты для работы с датами в строковом формате.
 */
public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DateUtils() {
        // Utility class
    }

    /**
     * Получить текущую дату в формате YYYY-MM-DD.
     *
     * @return текущая дата
     */
    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * Получить текущую дату и время в формате YYYY-MM-DD HH:MM:SS.
     *
     * @return текущая дата и время
     */
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    /**
     * Получить текущую дату и время в формате YYYY-MM-DD HH:MM.
     *
     * @return текущая дата и время (с точностью до минут)
     */
    public static String getCurrentDateTimeMinute() {
        return LocalDateTime.now().format(DATE_TIME_MINUTE_FORMATTER);
    }

    /**
     * Проверить, является ли первая дата раньше второй.
     *
     * @param date1 первая дата (YYYY-MM-DD)
     * @param date2 вторая дата (YYYY-MM-DD)
     * @return true, если date1 раньше date2
     */
    public static boolean isBefore(String date1, String date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        try {
            LocalDate d1 = parseDate(date1);
            LocalDate d2 = parseDate(date2);
            return d1.isBefore(d2);
        } catch (DateTimeParseException e) {
            // Если формат некорректен, используем строковое сравнение
            return date1.compareTo(date2) < 0;
        }
    }

    /**
     * Проверить, является ли первая дата и время раньше второй.
     *
     * @param dateTime1 первая дата и время (YYYY-MM-DD HH:MM)
     * @param dateTime2 вторая дата и время (YYYY-MM-DD HH:MM)
     * @return true, если dateTime1 раньше dateTime2
     */
    public static boolean isBeforeDateTime(String dateTime1, String dateTime2) {
        if (dateTime1 == null || dateTime2 == null) {
            return false;
        }
        try {
            LocalDateTime d1 = parseDateTime(dateTime1);
            LocalDateTime d2 = parseDateTime(dateTime2);
            return d1.isBefore(d2);
        } catch (DateTimeParseException e) {
            // Если формат некорректен, используем строковое сравнение
            return dateTime1.compareTo(dateTime2) < 0;
        }
    }

    /**
     * Проверить, является ли первая дата позже второй.
     *
     * @param date1 первая дата (YYYY-MM-DD)
     * @param date2 вторая дата (YYYY-MM-DD)
     * @return true, если date1 позже date2
     */
    public static boolean isAfter(String date1, String date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        try {
            LocalDate d1 = parseDate(date1);
            LocalDate d2 = parseDate(date2);
            return d1.isAfter(d2);
        } catch (DateTimeParseException e) {
            // Если формат некорректен, используем строковое сравнение
            return date1.compareTo(date2) > 0;
        }
    }

    /**
     * Проверить, является ли первая дата и время позже второй.
     *
     * @param dateTime1 первая дата и время (YYYY-MM-DD HH:MM)
     * @param dateTime2 вторая дата и время (YYYY-MM-DD HH:MM)
     * @return true, если dateTime1 позже dateTime2
     */
    public static boolean isAfterDateTime(String dateTime1, String dateTime2) {
        if (dateTime1 == null || dateTime2 == null) {
            return false;
        }
        try {
            LocalDateTime d1 = parseDateTime(dateTime1);
            LocalDateTime d2 = parseDateTime(dateTime2);
            return d1.isAfter(d2);
        } catch (DateTimeParseException e) {
            // Если формат некорректен, используем строковое сравнение
            return dateTime1.compareTo(dateTime2) > 0;
        }
    }

    /**
     * Проверить, являются ли даты равными.
     *
     * @param date1 первая дата (YYYY-MM-DD)
     * @param date2 вторая дата (YYYY-MM-DD)
     * @return true, если даты равны
     */
    public static boolean isEqual(String date1, String date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        try {
            LocalDate d1 = parseDate(date1);
            LocalDate d2 = parseDate(date2);
            return d1.isEqual(d2);
        } catch (DateTimeParseException e) {
            return date1.equals(date2);
        }
    }

    /**
     * Добавить дни к дате.
     *
     * @param date исходная дата (YYYY-MM-DD)
     * @param days количество дней для добавления (может быть отрицательным)
     * @return новая дата
     */
    public static String addDays(String date, int days) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        try {
            LocalDate localDate = parseDate(date);
            LocalDate result = localDate.plusDays(days);
            return result.format(DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected: yyyy-MM-dd", e);
        }
    }

    /**
     * Добавить часы к дате и времени.
     *
     * @param dateTime исходная дата и время (YYYY-MM-DD HH:MM)
     * @param hours количество часов для добавления (может быть отрицательным)
     * @return новая дата и время
     */
    public static String addHours(String dateTime, int hours) {
        if (dateTime == null) {
            throw new IllegalArgumentException("DateTime cannot be null");
        }
        try {
            LocalDateTime localDateTime = parseDateTime(dateTime);
            LocalDateTime result = localDateTime.plusHours(hours);
            return result.format(DATE_TIME_MINUTE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid datetime format. Expected: yyyy-MM-dd HH:mm", e);
        }
    }

    /**
     * Добавить минуты к дате и времени.
     *
     * @param dateTime исходная дата и время (YYYY-MM-DD HH:MM)
     * @param minutes количество минут для добавления (может быть отрицательным)
     * @return новая дата и время
     */
    public static String addMinutes(String dateTime, int minutes) {
        if (dateTime == null) {
            throw new IllegalArgumentException("DateTime cannot be null");
        }
        try {
            LocalDateTime localDateTime = parseDateTime(dateTime);
            LocalDateTime result = localDateTime.plusMinutes(minutes);
            return result.format(DATE_TIME_MINUTE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid datetime format. Expected: yyyy-MM-dd HH:mm", e);
        }
    }

    /**
     * Вычислить количество дней между двумя датами.
     *
     * @param date1 первая дата (YYYY-MM-DD)
     * @param date2 вторая дата (YYYY-MM-DD)
     * @return количество дней (положительное, если date2 позже date1)
     */
    public static long daysBetween(String date1, String date2) {
        if (date1 == null || date2 == null) {
            return 0;
        }
        try {
            LocalDate d1 = parseDate(date1);
            LocalDate d2 = parseDate(date2);
            return ChronoUnit.DAYS.between(d1, d2);
        } catch (DateTimeParseException e) {
            return 0;
        }
    }

    /**
     * Вычислить количество часов между двумя датами и временем.
     *
     * @param dateTime1 первая дата и время (YYYY-MM-DD HH:MM)
     * @param dateTime2 вторая дата и время (YYYY-MM-DD HH:MM)
     * @return количество часов (положительное, если dateTime2 позже dateTime1)
     */
    public static long hoursBetween(String dateTime1, String dateTime2) {
        if (dateTime1 == null || dateTime2 == null) {
            return 0;
        }
        try {
            LocalDateTime d1 = parseDateTime(dateTime1);
            LocalDateTime d2 = parseDateTime(dateTime2);
            return ChronoUnit.HOURS.between(d1, d2);
        } catch (DateTimeParseException e) {
            return 0;
        }
    }

    /**
     * Отформатировать относительное время (например, "2 days ago", "in 5 days").
     *
     * @param date дата для форматирования (YYYY-MM-DD)
     * @return отформатированная строка относительного времени
     */
    public static String formatRelativeTime(String date) {
        if (date == null) {
            return "Unknown";
        }

        try {
            LocalDate targetDate = parseDate(date);
            LocalDate today = LocalDate.now();
            long days = ChronoUnit.DAYS.between(today, targetDate);

            if (days == 0) {
                return "Today";
            } else if (days == 1) {
                return "Tomorrow";
            } else if (days == -1) {
                return "Yesterday";
            } else if (days > 0) {
                if (days <= 7) {
                    return "in " + days + " day" + (days > 1 ? "s" : "");
                } else if (days <= 30) {
                    long weeks = days / 7;
                    return "in " + weeks + " week" + (weeks > 1 ? "s" : "");
                } else {
                    long months = days / 30;
                    return "in " + months + " month" + (months > 1 ? "s" : "");
                }
            } else {
                days = Math.abs(days);
                if (days <= 7) {
                    return days + " day" + (days > 1 ? "s" : "") + " ago";
                } else if (days <= 30) {
                    long weeks = days / 7;
                    return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
                } else {
                    long months = days / 30;
                    return months + " month" + (months > 1 ? "s" : "") + " ago";
                }
            }
        } catch (IllegalArgumentException e) {
            return date;
        }
    }

    /**
     * Отформатировать относительное время для даты и времени.
     *
     * @param dateTime дата и время (YYYY-MM-DD HH:MM)
     * @return отформатированная строка относительного времени
     */
    public static String formatRelativeDateTime(String dateTime) {
        if (dateTime == null) {
            return "Unknown";
        }

        try {
            LocalDateTime targetDateTime = parseDateTime(dateTime);
            LocalDateTime now = LocalDateTime.now();

            long minutes = ChronoUnit.MINUTES.between(now, targetDateTime);

            if (Math.abs(minutes) < 60) {
                if (minutes == 0) {
                    return "Just now";
                } else if (minutes > 0) {
                    return "in " + minutes + " minute" + (minutes > 1 ? "s" : "");
                } else {
                    return Math.abs(minutes) + " minute" + (Math.abs(minutes) > 1 ? "s" : "") + " ago";
                }
            }

            long hours = ChronoUnit.HOURS.between(now, targetDateTime);

            if (Math.abs(hours) < 24) {
                if (hours > 0) {
                    return "in " + hours + " hour" + (hours > 1 ? "s" : "");
                } else {
                    return Math.abs(hours) + " hour" + (Math.abs(hours) > 1 ? "s" : "") + " ago";
                }
            }

            // Используем форматирование по дате
            return formatRelativeTime(targetDateTime.format(DATE_FORMATTER));
        } catch (DateTimeParseException e) {
            return dateTime;
        }
    }

    /**
     * Проверить, является ли дата валидной.
     *
     * @param date строка даты
     * @return true, если дата валидна
     */
    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDate.parse(date.trim(), DATE_FORMATTER);
            return true;
        } catch (java.time.format.DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Проверить, является ли дата и время валидными.
     *
     * @param dateTime строка даты и времени
     * @return true, если дата и время валидны
     */
    public static boolean isValidDateTime(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDateTime.parse(dateTime.trim(), DATE_TIME_MINUTE_FORMATTER);
            return true;
        } catch (java.time.format.DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Разобрать строку в LocalDate.
     *
     * @param date строка даты (YYYY-MM-DD)
     * @return LocalDate
     */
    public static LocalDate parseDate(String date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        try {
            return LocalDate.parse(date.trim(), DATE_FORMATTER);
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + date + ". Expected: YYYY-MM-DD", e);
        }
    }

    /**
     * Разобрать строку в LocalDateTime (с точностью до минут).
     *
     * @param dateTime строка даты и времени (YYYY-MM-DD HH:MM)
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("DateTime cannot be null");
        }
        try {
            return LocalDateTime.parse(dateTime.trim(), DATE_TIME_MINUTE_FORMATTER);
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid datetime format: " + dateTime + ". Expected: YYYY-MM-DD HH:MM", e);
        }
    }

    /**
     * Форматировать дату в другой формат.
     *
     * @param date исходная дата (YYYY-MM-DD)
     * @param newFormat новый формат
     * @return отформатированная дата
     */
    public static String formatDate(String date, String newFormat) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        try {
            LocalDate localDate = parseDate(date);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(newFormat);
            return localDate.format(formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected: yyyy-MM-dd", e);
        }
    }

    /**
     * Получить день недели для даты.
     *
     * @param date дата (YYYY-MM-DD)
     * @return название дня недели
     */
    public static String getDayOfWeek(String date) {
        if (date == null) {
            return "Unknown";
        }
        try {
            LocalDate localDate = parseDate(date);
            return localDate.getDayOfWeek().toString();
        } catch (IllegalArgumentException e) {
            return "Unknown";
        }
    }
}
