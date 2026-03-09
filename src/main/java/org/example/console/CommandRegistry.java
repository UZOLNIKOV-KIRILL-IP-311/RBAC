package org.example.console;

import org.example.manager.AssignmentManager;
import org.example.manager.RoleManager;
import org.example.manager.UserManager;
import org.example.rbac.*;
import org.example.util.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Регистратор всех команд системы.
 */
public class CommandRegistry {

    private final CommandParser parser;

    public CommandRegistry(CommandParser parser) {
        this.parser = parser;
        registerAllCommands();
    }

    private void registerAllCommands() {
        // User commands
        registerUserCommands();

        // Role commands
        registerRoleCommands();

        // Assignment commands
        registerAssignmentCommands();

        // Permission commands
        registerPermissionCommands();

        // System commands
        registerSystemCommands();
    }

    private void registerUserCommands() {
        parser.registerCommand("user-list", "List all users", (scanner, system) -> {
            List<User> users = system.getUserManager().findAll();
            if (users.isEmpty()) {
                System.out.println("No users found.");
                return;
            }
            List<String[]> rows = users.stream()
                    .map(u -> new String[]{u.username(), u.fullName(), u.email()})
                    .collect(Collectors.toList());
            System.out.println(FormatUtils.formatTable(new String[]{"Username", "Full Name", "Email"}, rows));
            System.out.println("Total: " + users.size() + " user(s)");
        });

        parser.registerCommand("user-create", "Create a new user", (scanner, system) -> {
            System.out.println("Enter user details:");
            String username = ConsoleUtils.promptString(scanner, "Username: ", true);
            if (!ValidationUtils.isValidUsername(username)) {
                ConsoleUtils.printError("Invalid username. Must be 3-20 chars, letters/numbers/underscore only.");
                return;
            }
            if (system.getUserManager().exists(username)) {
                ConsoleUtils.printError("User already exists.");
                return;
            }
            String fullName = ConsoleUtils.promptString(scanner, "Full Name: ", true);
            String email = ConsoleUtils.promptString(scanner, "Email: ", true);
            if (!ValidationUtils.isValidEmail(email)) {
                ConsoleUtils.printError("Invalid email format.");
                return;
            }
            try {
                User user = User.create(username, fullName, email);
                system.getUserManager().add(user);
                ConsoleUtils.printSuccess("User '" + username + "' created!");
            } catch (IllegalArgumentException e) {
                ConsoleUtils.printError(e.getMessage());
            }
        });

        parser.registerCommand("user-view", "View user details", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Username: ", true);
            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("User not found.");
                return;
            }
            User user = userOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);

            System.out.println(FormatUtils.formatBox(
                    "User: " + user.username() + "\n" +
                    "Full Name: " + user.fullName() + "\n" +
                    "Email: " + user.email()
            ));

            System.out.println("\nRoles:");
            if (assignments.isEmpty()) {
                System.out.println("  No roles assigned.");
            } else {
                for (RoleAssignment a : assignments) {
                    System.out.println("  - " + a.role().getName() + " [" + a.assignmentType() + "] " +
                            (a.isActive() ? "ACTIVE" : "INACTIVE"));
                }
            }
        });

        parser.registerCommand("user-update", "Update user data", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Username: ", true);
            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("User not found.");
                return;
            }
            User user = userOpt.get();
            System.out.println("Current: " + user.fullName() + " / " + user.email());
            String newFullName = ConsoleUtils.promptString(scanner, "New Full Name [" + user.fullName() + "]: ", user.fullName());
            String newEmail = ConsoleUtils.promptString(scanner, "New Email [" + user.email() + "]: ", user.email());
            if (!ValidationUtils.isValidEmail(newEmail)) {
                ConsoleUtils.printError("Invalid email format.");
                return;
            }
            try {
                system.getUserManager().update(username, newFullName, newEmail);
                ConsoleUtils.printSuccess("User updated!");
            } catch (IllegalArgumentException e) {
                ConsoleUtils.printError(e.getMessage());
            }
        });

        parser.registerCommand("user-delete", "Delete a user", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Username: ", true);
            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("User not found.");
                return;
            }
            if (!ConsoleUtils.promptYesNo(scanner, "Delete user '" + username + "'?")) {
                System.out.println("Cancelled.");
                return;
            }
            User user = userOpt.get();
            // Remove assignments first
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            for (RoleAssignment a : assignments) {
                system.getAssignmentManager().remove(a);
            }
            system.getUserManager().remove(user);
            ConsoleUtils.printSuccess("User deleted!");
        });

        parser.registerCommand("user-search", "Search users by filter", (scanner, system) -> {
            System.out.println("Search by:");
            System.out.println("  1. Username contains");
            System.out.println("  2. Email contains");
            System.out.println("  3. Email domain");
            System.out.println("  4. Full name contains");
            int choice = ConsoleUtils.promptInt(scanner, "Choice (1-4): ", 1, 4);

            UserFilter filter = null;
            switch (choice) {
                case 1:
                    String usernamePart = ConsoleUtils.promptString(scanner, "Username contains: ", true);
                    filter = UserFilters.byUsernameContains(usernamePart);
                    break;
                case 2:
                    String emailPart = ConsoleUtils.promptString(scanner, "Email contains: ", true);
                    filter = UserFilters.byEmailContains(emailPart);
                    break;
                case 3:
                    String domain = ConsoleUtils.promptString(scanner, "Domain: ", true);
                    filter = UserFilters.byEmailDomain(domain);
                    break;
                case 4:
                    String namePart = ConsoleUtils.promptString(scanner, "Full name contains: ", true);
                    filter = UserFilters.byFullNameContains(namePart);
                    break;
            }

            List<User> results = system.getUserManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("No users found.");
                return;
            }
            List<String[]> rows = results.stream()
                    .map(u -> new String[]{u.username(), u.fullName(), u.email()})
                    .collect(Collectors.toList());
            System.out.println(FormatUtils.formatTable(new String[]{"Username", "Full Name", "Email"}, rows));
            System.out.println("Found: " + results.size() + " user(s)");
        });
    }

    private void registerRoleCommands() {
        parser.registerCommand("role-list", "List all roles", (scanner, system) -> {
            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("No roles found.");
                return;
            }
            List<String[]> rows = roles.stream()
                    .map(r -> new String[]{r.getName(), String.valueOf(r.getPermissions().size()), r.getId().substring(0, 13) + "..."})
                    .collect(Collectors.toList());
            System.out.println(FormatUtils.formatTable(new String[]{"Name", "Permissions", "ID"}, rows));
            System.out.println("Total: " + roles.size() + " role(s)");
        });

        parser.registerCommand("role-create", "Create a new role", (scanner, system) -> {
            String name = ConsoleUtils.promptString(scanner, "Role Name: ", true);
            if (system.getRoleManager().exists(name)) {
                ConsoleUtils.printError("Role already exists.");
                return;
            }
            String description = ConsoleUtils.promptString(scanner, "Description: ", true);
            try {
                Role role = new Role(name, description);
                system.getRoleManager().add(role);
                ConsoleUtils.printSuccess("Role '" + name + "' created!");

                if (ConsoleUtils.promptYesNo(scanner, "Add permissions?")) {
                    addPermissionToRole(scanner, system, name);
                }
            } catch (IllegalArgumentException e) {
                ConsoleUtils.printError(e.getMessage());
            }
        });

        parser.registerCommand("role-view", "View role details", (scanner, system) -> {
            String name = ConsoleUtils.promptString(scanner, "Role Name: ", true);
            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Role not found.");
                return;
            }
            Role role = roleOpt.get();
            System.out.println(role.format());

            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            long activeCount = assignments.stream().filter(RoleAssignment::isActive).count();
            System.out.println("\nAssigned to " + activeCount + " user(s) (active)");
        });

        parser.registerCommand("role-update", "Update role", (scanner, system) -> {
            String name = ConsoleUtils.promptString(scanner, "Role Name: ", true);
            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Role not found.");
                return;
            }
            Role role = roleOpt.get();
            System.out.println("Current description: " + role.getDescription());
            String newDesc = ConsoleUtils.promptString(scanner, "New Description [" + role.getDescription() + "]: ", role.getDescription());
            // Role doesn't have setter, so we just inform
            ConsoleUtils.printInfo("Role update not fully implemented. Use role-delete and role-create instead.");
        });

        parser.registerCommand("role-delete", "Delete a role", (scanner, system) -> {
            String name = ConsoleUtils.promptString(scanner, "Role Name: ", true);
            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Role not found.");
                return;
            }
            Role role = roleOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            long activeCount = assignments.stream().filter(RoleAssignment::isActive).count();
            if (activeCount > 0) {
                ConsoleUtils.printWarning("Role is assigned to " + activeCount + " user(s)!");
                if (!ConsoleUtils.promptYesNo(scanner, "Continue anyway?")) {
                    System.out.println("Cancelled.");
                    return;
                }
            }
            if (!ConsoleUtils.promptYesNo(scanner, "Delete role '" + name + "'?")) {
                System.out.println("Cancelled.");
                return;
            }
            system.getRoleManager().remove(role);
            ConsoleUtils.printSuccess("Role deleted!");
        });

        parser.registerCommand("role-add-permission", "Add permission to role", (scanner, system) -> {
            String roleName = ConsoleUtils.promptString(scanner, "Role Name: ", true);
            addPermissionToRole(scanner, system, roleName);
        });

        parser.registerCommand("role-remove-permission", "Remove permission from role", (scanner, system) -> {
            String roleName = ConsoleUtils.promptString(scanner, "Role Name: ", true);
            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Role not found.");
                return;
            }
            Role role = roleOpt.get();
            Set<Permission> permissions = role.getPermissions();
            if (permissions.isEmpty()) {
                ConsoleUtils.printInfo("No permissions in this role.");
                return;
            }
            List<String> permList = permissions.stream()
                    .map(p -> p.name() + " on " + p.resource())
                    .collect(Collectors.toList());
            String selected = ConsoleUtils.promptChoice(scanner, "Select permission to remove:", permList, "Cancel");
            if (selected == null) return;
            String[] parts = selected.split(" on ", 2);
            Permission perm = Permission.create(parts[0], parts[1], "");
            system.getRoleManager().removePermissionFromRole(roleName, perm);
            ConsoleUtils.printSuccess("Permission removed!");
        });

        parser.registerCommand("role-search", "Search roles", (scanner, system) -> {
            System.out.println("Search by:");
            System.out.println("  1. Name contains");
            System.out.println("  2. Has permission");
            System.out.println("  3. Min permissions count");
            int choice = ConsoleUtils.promptInt(scanner, "Choice (1-3): ", 1, 3);

            RoleFilter filter = null;
            switch (choice) {
                case 1:
                    String namePart = ConsoleUtils.promptString(scanner, "Name contains: ", true);
                    filter = RoleFilters.byNameContains(namePart);
                    break;
                case 2:
                    String permName = ConsoleUtils.promptString(scanner, "Permission name: ", true).toUpperCase();
                    String resource = ConsoleUtils.promptString(scanner, "Resource: ", true).toLowerCase();
                    filter = RoleFilters.hasPermission(permName, resource);
                    break;
                case 3:
                    int minCount = ConsoleUtils.promptInt(scanner, "Min permissions: ", 0, 100);
                    filter = RoleFilters.hasAtLeastNPermissions(minCount);
                    break;
            }

            List<Role> results = system.getRoleManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("No roles found.");
                return;
            }
            List<String[]> rows = results.stream()
                    .map(r -> new String[]{r.getName(), String.valueOf(r.getPermissions().size()), r.getDescription()})
                    .collect(Collectors.toList());
            System.out.println(FormatUtils.formatTable(new String[]{"Name", "Permissions", "Description"}, rows));
            System.out.println("Found: " + results.size() + " role(s)");
        });
    }

    private void addPermissionToRole(Scanner scanner, RBACSystem system, String roleName) {
        Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
        if (roleOpt.isEmpty()) {
            ConsoleUtils.printError("Role not found.");
            return;
        }
        String permName = ConsoleUtils.promptString(scanner, "Permission Name (e.g., READ): ", true).toUpperCase();
        String resource = ConsoleUtils.promptString(scanner, "Resource (e.g., USERS): ", true).toLowerCase();
        String description = ConsoleUtils.promptString(scanner, "Description: ", true);
        try {
            Permission perm = Permission.create(permName, resource, description);
            system.getRoleManager().addPermissionToRole(roleName, perm);
            ConsoleUtils.printSuccess("Permission added!");
        } catch (IllegalArgumentException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void registerAssignmentCommands() {
        parser.registerCommand("assign-role", "Assign role to user", (scanner, system) -> {
            List<User> users = system.getUserManager().findAll();
            if (users.isEmpty()) {
                ConsoleUtils.printError("No users. Create a user first.");
                return;
            }
            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                ConsoleUtils.printError("No roles. Create a role first.");
                return;
            }

            User user = ConsoleUtils.promptChoice(scanner, "Select user:", users, "Cancel");
            if (user == null) return;

            Role role = ConsoleUtils.promptChoice(scanner, "Select role:", roles, "Cancel");
            if (role == null) return;

            if (system.getAssignmentManager().userHasRole(user, role)) {
                ConsoleUtils.printError("User already has this role.");
                return;
            }

            System.out.println("Assignment type:");
            System.out.println("  1. Permanent");
            System.out.println("  2. Temporary");
            int typeChoice = ConsoleUtils.promptInt(scanner, "Choice (1-2): ", 1, 2);

            String reason = ConsoleUtils.promptString(scanner, "Reason (optional): ", "");
            AssignmentMetadata metadata = AssignmentMetadata.now(system.getCurrentUser(), reason);

            try {
                if (typeChoice == 1) {
                    PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
                    system.getAssignmentManager().add(assignment);
                    ConsoleUtils.printSuccess("Role assigned (permanent)!");
                } else {
                    String expiresAt = ConsoleUtils.promptString(scanner, "Expiration (yyyy-MM-dd HH:mm): ", true);
                    if (!ValidationUtils.isValidDateTime(expiresAt)) {
                        ConsoleUtils.printError("Invalid date format.");
                        return;
                    }
                    TemporaryAssignment assignment = new TemporaryAssignment(user, role, metadata, expiresAt);
                    system.getAssignmentManager().add(assignment);
                    ConsoleUtils.printSuccess("Role assigned (temporary, expires: " + expiresAt + ")!");
                }
            } catch (IllegalArgumentException e) {
                ConsoleUtils.printError(e.getMessage());
            }
        });

        parser.registerCommand("revoke-role", "Revoke role from user", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Username: ", true);
            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("User not found.");
                return;
            }
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(userOpt.get())
                    .stream().filter(RoleAssignment::isActive).collect(Collectors.toList());
            if (assignments.isEmpty()) {
                ConsoleUtils.printInfo("No active assignments.");
                return;
            }
            List<String> assignList = assignments.stream()
                    .map(a -> a.role().getName() + " [" + a.assignmentType() + "]")
                    .collect(Collectors.toList());
            String selected = ConsoleUtils.promptChoice(scanner, "Select assignment to revoke:", assignList, "Cancel");
            if (selected == null) return;

            for (RoleAssignment a : assignments) {
                String desc = a.role().getName() + " [" + a.assignmentType() + "]";
                if (desc.equals(selected)) {
                    if (a instanceof PermanentAssignment) {
                        ((PermanentAssignment) a).revoke();
                    } else {
                        system.getAssignmentManager().remove(a);
                    }
                    ConsoleUtils.printSuccess("Role revoked!");
                    return;
                }
            }
        });

        parser.registerCommand("assignment-list", "List all assignments", (scanner, system) -> {
            List<RoleAssignment> assignments = system.getAssignmentManager().findAll();
            if (assignments.isEmpty()) {
                System.out.println("No assignments.");
                return;
            }
            List<String[]> rows = assignments.stream()
                    .map(a -> new String[]{
                            a.user().username(),
                            a.role().getName(),
                            a.assignmentType(),
                            a.isActive() ? "ACTIVE" : "INACTIVE",
                            a.metadata().assignedAt()
                    })
                    .collect(Collectors.toList());
            System.out.println(FormatUtils.formatTable(
                    new String[]{"Username", "Role", "Type", "Status", "Assigned At"}, rows));
            System.out.println("Total: " + assignments.size() + " assignment(s)");
        });

        parser.registerCommand("assignment-list-user", "List assignments for a user", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Username: ", true);
            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("User not found.");
                return;
            }
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(userOpt.get());
            if (assignments.isEmpty()) {
                System.out.println("No assignments for this user.");
                return;
            }
            for (RoleAssignment a : assignments) {
                System.out.println(a.summary());
            }
        });

        parser.registerCommand("assignment-list-role", "List users with a role", (scanner, system) -> {
            String roleName = ConsoleUtils.promptString(scanner, "Role Name: ", true);
            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Role not found.");
                return;
            }
            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(roleOpt.get());
            if (assignments.isEmpty()) {
                System.out.println("No users with this role.");
                return;
            }
            List<String[]> rows = assignments.stream()
                    .map(a -> new String[]{a.user().username(), a.assignmentType(), a.isActive() ? "ACTIVE" : "INACTIVE"})
                    .collect(Collectors.toList());
            System.out.println(FormatUtils.formatTable(new String[]{"Username", "Type", "Status"}, rows));
        });

        parser.registerCommand("assignment-active", "List active assignments", (scanner, system) -> {
            List<RoleAssignment> active = system.getAssignmentManager().getActiveAssignments();
            if (active.isEmpty()) {
                System.out.println("No active assignments.");
                return;
            }
            List<String[]> rows = active.stream()
                    .map(a -> new String[]{a.user().username(), a.role().getName(), a.assignmentType()})
                    .collect(Collectors.toList());
            System.out.println(FormatUtils.formatTable(new String[]{"Username", "Role", "Type"}, rows));
            System.out.println("Total: " + active.size() + " active assignment(s)");
        });

        parser.registerCommand("assignment-expired", "List expired assignments", (scanner, system) -> {
            List<RoleAssignment> expired = system.getAssignmentManager().getExpiredAssignments();
            if (expired.isEmpty()) {
                System.out.println("No expired assignments.");
                return;
            }
            List<String[]> rows = expired.stream()
                    .map(a -> new String[]{a.user().username(), a.role().getName(), a.metadata().assignedAt()})
                    .collect(Collectors.toList());
            System.out.println(FormatUtils.formatTable(new String[]{"Username", "Role", "Assigned At"}, rows));
            System.out.println("Total: " + expired.size() + " expired assignment(s)");
        });

        parser.registerCommand("assignment-extend", "Extend temporary assignment", (scanner, system) -> {
            String assignmentId = ConsoleUtils.promptString(scanner, "Assignment ID: ", true);
            Optional<RoleAssignment> assignmentOpt = system.getAssignmentManager().findById(assignmentId);
            if (assignmentOpt.isEmpty()) {
                ConsoleUtils.printError("Assignment not found.");
                return;
            }
            RoleAssignment assignment = assignmentOpt.get();
            if (!(assignment instanceof TemporaryAssignment)) {
                ConsoleUtils.printError("Not a temporary assignment.");
                return;
            }
            String newDate = ConsoleUtils.promptString(scanner, "New expiration (yyyy-MM-dd HH:mm): ", true);
            if (!ValidationUtils.isValidDateTime(newDate)) {
                ConsoleUtils.printError("Invalid date format.");
                return;
            }
            ((TemporaryAssignment) assignment).extend(newDate);
            ConsoleUtils.printSuccess("Assignment extended to " + newDate + "!");
        });

        parser.registerCommand("assignment-search", "Search assignments by filter", (scanner, system) -> {
            System.out.println("Search by:");
            System.out.println("  1. By user");
            System.out.println("  2. By role");
            System.out.println("  3. By type");
            System.out.println("  4. By status");
            int choice = ConsoleUtils.promptInt(scanner, "Choice (1-4): ", 1, 4);

            AssignmentFilter filter = null;
            switch (choice) {
                case 1:
                    String username = ConsoleUtils.promptString(scanner, "Username: ", true);
                    filter = AssignmentFilters.byUsername(username);
                    break;
                case 2:
                    String roleName = ConsoleUtils.promptString(scanner, "Role Name: ", true);
                    filter = AssignmentFilters.byRoleName(roleName);
                    break;
                case 3:
                    System.out.println("  1. Permanent");
                    System.out.println("  2. Temporary");
                    int typeChoice = ConsoleUtils.promptInt(scanner, "Type (1-2): ", 1, 2);
                    filter = AssignmentFilters.byType(typeChoice == 1 ? "PERMANENT" : "TEMPORARY");
                    break;
                case 4:
                    System.out.println("  1. Active");
                    System.out.println("  2. Inactive");
                    int statusChoice = ConsoleUtils.promptInt(scanner, "Status (1-2): ", 1, 2);
                    filter = statusChoice == 1 ? AssignmentFilters.activeOnly() : AssignmentFilters.inactiveOnly();
                    break;
            }

            List<RoleAssignment> results = system.getAssignmentManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("No assignments found.");
                return;
            }
            List<String[]> rows = results.stream()
                    .map(a -> new String[]{a.user().username(), a.role().getName(), a.assignmentType(), a.isActive() ? "ACTIVE" : "INACTIVE"})
                    .collect(Collectors.toList());
            System.out.println(FormatUtils.formatTable(new String[]{"Username", "Role", "Type", "Status"}, rows));
            System.out.println("Found: " + results.size() + " assignment(s)");
        });
    }

    private void registerPermissionCommands() {
        parser.registerCommand("permissions-user", "List all permissions of a user", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Username: ", true);
            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("User not found.");
                return;
            }
            Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(userOpt.get());
            if (permissions.isEmpty()) {
                System.out.println("No permissions.");
                return;
            }
            // Group by resource
            Map<String, List<Permission>> byResource = permissions.stream()
                    .collect(Collectors.groupingBy(Permission::resource));

            for (Map.Entry<String, List<Permission>> entry : byResource.entrySet()) {
                System.out.println(ConsoleUtils.ANSI_BOLD + entry.getKey() + ":" + ConsoleUtils.ANSI_RESET);
                for (Permission p : entry.getValue()) {
                    System.out.println("  - " + p.name() + ": " + p.description());
                }
            }
        });

        parser.registerCommand("permissions-check", "Check if user has a permission", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Username: ", true);
            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("User not found.");
                return;
            }
            String permName = ConsoleUtils.promptString(scanner, "Permission name: ", true).toUpperCase();
            String resource = ConsoleUtils.promptString(scanner, "Resource: ", true).toLowerCase();

            boolean hasPermission = system.getAssignmentManager().userHasPermission(userOpt.get(), permName, resource);
            if (hasPermission) {
                ConsoleUtils.printSuccess("User HAS permission '" + permName + "' on '" + resource + "'");
            } else {
                ConsoleUtils.printError("User does NOT have permission '" + permName + "' on '" + resource + "'");
            }
        });
    }

    private void registerSystemCommands() {
        parser.registerCommand("help", "Show help message", (scanner, system) -> {
            parser.printHelp();
        });

        parser.registerCommand("stats", "Show system statistics", (scanner, system) -> {
            System.out.println(system.generateStatistics());
        });

        parser.registerCommand("clear", "Clear console", (scanner, system) -> {
            ConsoleUtils.clearConsole();
        });

        parser.registerCommand("exit", "Exit the application", (scanner, system) -> {
            if (ConsoleUtils.promptYesNo(scanner, "Exit?")) {
                System.exit(0);
            }
        });

        parser.registerCommand("save", "Save data to file", (scanner, system) -> {
            String filename = ConsoleUtils.promptString(scanner, "Filename [rbac-data.ser]: ", "rbac-data.ser");
            // Simple serialization placeholder
            ConsoleUtils.printInfo("Save functionality - placeholder. Data persistence not fully implemented.");
        });

        parser.registerCommand("load", "Load data from file", (scanner, system) -> {
            String filename = ConsoleUtils.promptString(scanner, "Filename [rbac-data.ser]: ", "rbac-data.ser");
            // Simple deserialization placeholder
            ConsoleUtils.printInfo("Load functionality - placeholder. Data persistence not fully implemented.");
        });
    }
}
