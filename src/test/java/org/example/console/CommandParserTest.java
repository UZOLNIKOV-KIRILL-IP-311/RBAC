package org.example.console;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {

    private CommandParser parser;
    private RBACSystem system;
    private Scanner scanner;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = new RBACSystem();
        scanner = new Scanner(System.in);
    }

    @Test
    void testRegisterCommand() {
        Command cmd = (s, sys) -> {};
        parser.registerCommand("test", "Test command", cmd);
        assertTrue(parser.hasCommand("test"));
        assertTrue(parser.hasCommand("TEST"));
    }

    @Test
    void testRegisterCommandNullName() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.registerCommand(null, "desc", (s, sys) -> {}));
    }

    @Test
    void testRegisterCommandEmptyName() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.registerCommand("", "desc", (s, sys) -> {}));
    }

    @Test
    void testRegisterCommandNullCommand() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.registerCommand("test", "desc", null));
    }

    @Test
    void testExecuteCommand() {
        boolean[] executed = {false};
        parser.registerCommand("test", "Test", (s, sys) -> executed[0] = true);
        parser.executeCommand("test", scanner, system);
        assertTrue(executed[0]);
    }

    @Test
    void testExecuteCommandUnknown() {
        parser.executeCommand("unknown", scanner, system);
        // Should not throw, just print error
    }

    @Test
    void testExecuteCommandNull() {
        parser.executeCommand(null, scanner, system);
        // Should not throw
    }

    @Test
    void testExecuteCommandEmpty() {
        parser.executeCommand("", scanner, system);
        // Should not throw
    }

    @Test
    void testGetRegisteredCommands() {
        parser.registerCommand("cmd1", "Desc 1", (s, sys) -> {});
        parser.registerCommand("cmd2", "Desc 2", (s, sys) -> {});
        String[] commands = parser.getRegisteredCommands();
        assertEquals(2, commands.length);
    }

    @Test
    void testHasCommand() {
        assertFalse(parser.hasCommand("test"));
        parser.registerCommand("test", "Desc", (s, sys) -> {});
        assertTrue(parser.hasCommand("test"));
        assertFalse(parser.hasCommand("unknown"));
    }

    @Test
    void testParseAndExecute() {
        boolean[] executed = {false};
        parser.registerCommand("test", "Test", (s, sys) -> executed[0] = true);
        parser.parseAndExecute("test", scanner, system);
        assertTrue(executed[0]);
    }

    @Test
    void testParseAndExecuteWithArgs() {
        boolean[] executed = {false};
        parser.registerCommand("test", "Test", (s, sys) -> executed[0] = true);
        parser.parseAndExecute("test arg1 arg2", scanner, system);
        assertTrue(executed[0]);
    }

    @Test
    void testParseAndExecuteEmpty() {
        parser.parseAndExecute("", scanner, system);
        // Should not throw
    }

    @Test
    void testParseAndExecuteNull() {
        parser.parseAndExecute(null, scanner, system);
        // Should not throw
    }

    @Test
    void testPrintHelp() {
        parser.registerCommand("user-test", "User test command", (s, sys) -> {});
        parser.registerCommand("role-test", "Role test command", (s, sys) -> {});
        parser.registerCommand("assignment-test", "Assignment test", (s, sys) -> {});
        parser.registerCommand("permissions-test", "Permissions test", (s, sys) -> {});
        parser.registerCommand("help", "Help command", (s, sys) -> {});
        // Should not throw
        assertDoesNotThrow(() -> parser.printHelp());
    }

    @Test
    void testCategorizeCommand() {
        parser.registerCommand("user-list", "List users", (s, sys) -> {});
        parser.registerCommand("role-create", "Create role", (s, sys) -> {});
        parser.registerCommand("assign-role", "Assign role", (s, sys) -> {});
        parser.registerCommand("assignment-list", "List assignments", (s, sys) -> {});
        parser.registerCommand("permissions-user", "User permissions", (s, sys) -> {});
        parser.registerCommand("help", "Help", (s, sys) -> {});
        parser.registerCommand("stats", "Statistics", (s, sys) -> {});

        assertTrue(parser.hasCommand("user-list"));
        assertTrue(parser.hasCommand("role-create"));
        assertTrue(parser.hasCommand("assign-role"));
        assertTrue(parser.hasCommand("assignment-list"));
        assertTrue(parser.hasCommand("permissions-user"));
        assertTrue(parser.hasCommand("help"));
        assertTrue(parser.hasCommand("stats"));
    }
}
