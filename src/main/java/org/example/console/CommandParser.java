package org.example.console;

import org.example.util.ConsoleUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Парсер команд для консольной утилиты.
 */
public class CommandParser {

    private final Map<String, Command> commands;
    private final Map<String, String> commandDescriptions;

    public CommandParser() {
        this.commands = new HashMap<>();
        this.commandDescriptions = new HashMap<>();
    }

    /**
     * Зарегистрировать команду.
     *
     * @param name имя команды
     * @param description описание команды
     * @param command реализация команды
     */
    public void registerCommand(String name, String description, Command command) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Command name cannot be empty");
        }
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }

        commands.put(name.toLowerCase(), command);
        commandDescriptions.put(name.toLowerCase(), description != null ? description : "No description");
    }

    /**
     * Выполнить команду по имени.
     *
     * @param commandName имя команды
     * @param scanner сканер для ввода
     * @param system система RBAC
     */
    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        if (commandName == null || commandName.trim().isEmpty()) {
            ConsoleUtils.printError("Command name cannot be empty");
            return;
        }

        String normalizedCommand = commandName.toLowerCase().trim();
        Command command = commands.get(normalizedCommand);

        if (command == null) {
            ConsoleUtils.printError("Unknown command: " + commandName + ". Type 'help' for available commands.");
            return;
        }

        try {
            command.execute(scanner, system);
        } catch (Exception e) {
            ConsoleUtils.printError("Error executing command: " + e.getMessage());
        }
    }

    /**
     * Вывести справку по всем командам.
     */
    public void printHelp() {
        System.out.println();
        System.out.println(ConsoleUtils.ANSI_BOLD + "=== Available Commands ===" + ConsoleUtils.ANSI_RESET);
        System.out.println();

        Map<String, StringBuilder> categories = new HashMap<>();
        categories.put("User Management", new StringBuilder());
        categories.put("Role Management", new StringBuilder());
        categories.put("Assignment Management", new StringBuilder());
        categories.put("Permissions", new StringBuilder());
        categories.put("System", new StringBuilder());

        for (Map.Entry<String, String> entry : commandDescriptions.entrySet()) {
            String name = entry.getKey();
            String description = entry.getValue();

            String category = categorizeCommand(name);
            categories.get(category).append("  ")
                    .append(String.format("%-25s", name))
                    .append(" - ")
                    .append(description)
                    .append("\n");
        }

        String[] categoryOrder = {"User Management", "Role Management", "Assignment Management", "Permissions", "System"};
        String[] categoryColors = {
                ConsoleUtils.ANSI_YELLOW,
                ConsoleUtils.ANSI_CYAN,
                ConsoleUtils.ANSI_GREEN,
                ConsoleUtils.ANSI_BLUE,
                ConsoleUtils.ANSI_WHITE
        };

        for (int i = 0; i < categoryOrder.length; i++) {
            String categoryName = categoryOrder[i];
            StringBuilder content = categories.get(categoryName);
            if (content.length() > 0) {
                System.out.println(categoryColors[i] + "--- " + categoryName + " ---" + ConsoleUtils.ANSI_RESET);
                String output = content.toString();
                if (output.endsWith("\n")) {
                    output = output.substring(0, output.length() - 1);
                }
                System.out.println(output);
                System.out.println();
            }
        }
    }

    private String categorizeCommand(String commandName) {
        if (commandName.startsWith("user-")) {
            return "User Management";
        } else if (commandName.startsWith("role-")) {
            return "Role Management";
        } else if (commandName.startsWith("assign-") || commandName.startsWith("assignment-")) {
            return "Assignment Management";
        } else if (commandName.startsWith("permissions-")) {
            return "Permissions";
        } else {
            return "System";
        }
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        executeCommand(commandName, scanner, system);
    }

    public String[] getRegisteredCommands() {
        return commands.keySet().toArray(new String[0]);
    }

    public boolean hasCommand(String commandName) {
        return commands.containsKey(commandName.toLowerCase());
    }
}
