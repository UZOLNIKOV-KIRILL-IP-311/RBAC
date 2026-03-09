package org.example;

import org.example.console.*;
import org.example.util.ConsoleUtils;

import java.util.Scanner;

/**
 * Main entry point for the RBAC Console application.
 */
public class Main {
    public static void main(String[] args) {
        RBACSystem system = new RBACSystem();
        system.initialize();

        CommandParser parser = new CommandParser();
        new CommandRegistry(parser);

        Scanner scanner = new Scanner(System.in);

        ConsoleUtils.printSection("RBAC Management Console");
        System.out.println("Welcome to RBAC Management System!");
        System.out.println("Type 'help' for available commands or 'exit' to quit.\n");

        boolean running = true;
        while (running) {
            System.out.print(ConsoleUtils.ANSI_CYAN + "rbac> " + ConsoleUtils.ANSI_RESET);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            if ("exit".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input)) {
                if (ConsoleUtils.promptYesNo(scanner, "Exit?")) {
                    running = false;
                }
            } else {
                parser.parseAndExecute(input, scanner, system);
            }
        }

        ConsoleUtils.printInfo("Goodbye!");
        scanner.close();
    }
}
