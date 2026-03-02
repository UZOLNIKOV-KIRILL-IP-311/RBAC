package org.example.util;

import java.util.List;
import java.util.Scanner;

/**
 * Утилиты для интерактивного консольного ввода.
 */
public class ConsoleUtils {

    // ANSI color codes
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BOLD = "\u001B[1m";

    private ConsoleUtils() {
        // Utility class
    }

    /**
     * Запросить строку у пользователя.
     *
     * @param scanner сканер для ввода
     * @param message сообщение для вывода
     * @param required если true, пустая строка не допускается
     * @return введённая строка
     */
    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine();

            if (input == null || input.trim().isEmpty()) {
                if (required) {
                    System.out.println(ANSI_RED + "This field is required. Please try again." + ANSI_RESET);
                    continue;
                }
                return "";
            }

            return input.trim();
        }
    }

    /**
     * Запросить строку у пользователя с подсказкой по умолчанию.
     *
     * @param scanner сканер для ввода
     * @param message сообщение для вывода
     * @param defaultValue значение по умолчанию
     * @return введённая строка или значение по умолчанию
     */
    public static String promptString(Scanner scanner, String message, String defaultValue) {
        String messageWithDefault = message + (defaultValue != null ? " [" + defaultValue + "]" : "") + ": ";
        String input = promptString(scanner, messageWithDefault, false);

        if (input.isEmpty() && defaultValue != null) {
            return defaultValue;
        }

        return input;
    }

    /**
     * Запросить целое число в диапазоне.
     *
     * @param scanner сканер для ввода
     * @param message сообщение для вывода
     * @param min минимальное значение
     * @param max максимальное значение
     * @return введённое число
     */
    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine();

            try {
                int value = Integer.parseInt(input.trim());

                if (value < min || value > max) {
                    System.out.println(ANSI_RED + "Please enter a number between " + min + " and " + max + "." + ANSI_RESET);
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                System.out.println(ANSI_RED + "Invalid number. Please try again." + ANSI_RESET);
            }
        }
    }

    /**
     * Запросить подтверждение (yes/no).
     *
     * @param scanner сканер для ввода
     * @param message сообщение для вывода
     * @return true если пользователь ввёл yes/y, false если no/n
     */
    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " (yes/no): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if ("yes".equals(input) || "y".equals(input)) {
                return true;
            } else if ("no".equals(input) || "n".equals(input)) {
                return false;
            } else {
                System.out.println(ANSI_YELLOW + "Please enter 'yes' or 'no'." + ANSI_RESET);
            }
        }
    }

    /**
     * Выбрать элемент из списка.
     *
     * @param scanner сканер для ввода
     * @param message сообщение для вывода
     * @param options список опций
     * @param <T> тип элементов
     * @return выбранный элемент
     */
    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        return promptChoice(scanner, message, options, null);
    }

    /**
     * Выбрать элемент из списка с опцией отмены.
     *
     * @param scanner сканер для ввода
     * @param message сообщение для вывода
     * @param options список опций
     * @param cancelOption текст опции отмены (если null, отмена недоступна)
     * @param <T> тип элементов
     * @return выбранный элемент или null если выбрана отмена
     */
    public static <T> T promptChoice(Scanner scanner, String message, List<T> options, String cancelOption) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Options list cannot be empty");
        }

        while (true) {
            System.out.println();
            System.out.println(ANSI_BOLD + message + ANSI_RESET);

            for (int i = 0; i < options.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + options.get(i).toString());
            }

            if (cancelOption != null) {
                System.out.println("  0. " + cancelOption);
            }

            System.out.print("Enter choice (1-" + options.size() + (cancelOption != null ? ", 0" : "") + "): ");

            String input = scanner.nextLine().trim();

            if ("0".equals(input) && cancelOption != null) {
                return null;
            }

            try {
                int choice = Integer.parseInt(input);

                if (choice < 1 || choice > options.size()) {
                    System.out.println(ANSI_RED + "Invalid choice. Please try again." + ANSI_RESET);
                    continue;
                }

                return options.get(choice - 1);
            } catch (NumberFormatException e) {
                System.out.println(ANSI_RED + "Please enter a valid number." + ANSI_RESET);
            }
        }
    }

    /**
     * Запросить число с минимальным значением.
     *
     * @param scanner сканер для ввода
     * @param message сообщение для вывода
     * @param min минимальное значение
     * @return введённое число
     */
    public static int promptIntMin(Scanner scanner, String message, int min) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine();

            try {
                int value = Integer.parseInt(input.trim());

                if (value < min) {
                    System.out.println(ANSI_RED + "Please enter a number greater than or equal to " + min + "." + ANSI_RESET);
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                System.out.println(ANSI_RED + "Invalid number. Please try again." + ANSI_RESET);
            }
        }
    }

    /**
     * Вывести сообщение об успехе.
     *
     * @param message сообщение
     */
    public static void printSuccess(String message) {
        System.out.println(ANSI_GREEN + "✓ " + message + ANSI_RESET);
    }

    /**
     * Вывести сообщение об ошибке.
     *
     * @param message сообщение
     */
    public static void printError(String message) {
        System.out.println(ANSI_RED + "✗ Error: " + message + ANSI_RESET);
    }

    /**
     * Вывести предупреждение.
     *
     * @param message сообщение
     */
    public static void printWarning(String message) {
        System.out.println(ANSI_YELLOW + "⚠ " + message + ANSI_RESET);
    }

    /**
     * Вывести информационное сообщение.
     *
     * @param message сообщение
     */
    public static void printInfo(String message) {
        System.out.println(ANSI_BLUE + "ℹ " + message + ANSI_RESET);
    }

    /**
     * Вывести заголовок секции.
     *
     * @param title заголовок
     */
    public static void printSection(String title) {
        System.out.println();
        System.out.println(ANSI_BOLD + ANSI_CYAN + "=== " + title + " ===" + ANSI_RESET);
        System.out.println();
    }

    /**
     * Очистить консоль (работает не во всех средах).
     */
    public static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Игнорируем ошибки очистки консоли
        }
    }
}
