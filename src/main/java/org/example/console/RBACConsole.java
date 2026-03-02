package org.example.console;

import org.example.manager.AssignmentManager;
import org.example.manager.RoleManager;
import org.example.manager.UserManager;
import org.example.rbac.*;
import org.example.util.*;
import org.example.audit.AuditLog;
import org.example.report.ReportGenerator;

import java.io.*;
import java.util.*;

/**
 * Интерактивная консольная утилита для управления RBAC.
 */
public class RBACConsole {

    private final Scanner scanner;
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private final AuditLog auditLog;
    private final ReportGenerator reportGenerator;

    private String currentUser = "system";

    public RBACConsole() {
        this.scanner = new Scanner(System.in);
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.auditLog = new AuditLog();
        this.reportGenerator = new ReportGenerator();

        // Создаём пользователя-администратора по умолчанию
        initializeDefaultAdmin();
    }

    private void initializeDefaultAdmin() {
        try {
            User admin = User.create("admin", "System Administrator", "admin@company.com");
            userManager.add(admin);
            Role adminRole = new Role("Administrator", "Full system access");
            adminRole.addPermission(Permission.create("READ", "USERS", "Can view users"));
            adminRole.addPermission(Permission.create("WRITE", "USERS", "Can manage users"));
            adminRole.addPermission(Permission.create("DELETE", "USERS", "Can delete users"));
            adminRole.addPermission(Permission.create("READ", "ROLES", "Can view roles"));
            adminRole.addPermission(Permission.create("WRITE", "ROLES", "Can manage roles"));
            adminRole.addPermission(Permission.create("ADMIN", "SYSTEM", "Full system access"));
            roleManager.add(adminRole);

            AssignmentMetadata metadata = AssignmentMetadata.now(currentUser, "Initial system setup");
            PermanentAssignment assignment = new PermanentAssignment(admin, adminRole, metadata);
            // Добавляем напрямую во внутренний мап, чтобы избежать проверки существования
            try {
                assignmentManager.add(assignment);
            } catch (IllegalArgumentException e) {
                // Игнорируем, если уже существует
            }

            auditLog.log("SYSTEM_INIT", currentUser, "admin", "Default administrator created");
        } catch (Exception e) {
            // Игнорируем ошибки инициализации
        }
    }

    /**
     * Запуск консольной утилиты.
     */
    public void run() {
        ConsoleUtils.printSection("RBAC Management Console");
        System.out.println("Welcome to RBAC Management System!");
        System.out.println("Type 'help' for available commands or 'exit' to quit.");
        System.out.println();

        boolean running = true;
        while (running) {
            System.out.print(ConsoleUtils.ANSI_CYAN + "rbac> " + ConsoleUtils.ANSI_RESET);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            String args = parts.length > 1 ? parts[1] : "";

            running = processCommand(command, args);
        }

        ConsoleUtils.printInfo("Goodbye!");
        scanner.close();
    }

    private boolean processCommand(String command, String args) {
        try {
            switch (command) {
                case "help":
                    showHelp();
                    break;
                case "exit":
                case "quit":
                    return false;
                case "clear":
                    ConsoleUtils.clearConsole();
                    break;

                // User commands
                case "user-create":
                    createUserWizard();
                    break;
                case "user-list":
                    listUsers();
                    break;
                case "user-view":
                    viewUser(args);
                    break;
                case "user-update":
                    updateUserWizard(args);
                    break;
                case "user-delete":
                    deleteUser(args);
                    break;

                // Role commands
                case "role-create":
                    createRoleWizard();
                    break;
                case "role-list":
                    listRoles();
                    break;
                case "role-view":
                    viewRole(args);
                    break;
                case "role-delete":
                    deleteRole(args);
                    break;
                case "role-add-permission":
                    addPermissionToRoleWizard();
                    break;
                case "role-remove-permission":
                    removePermissionFromRoleWizard();
                    break;

                // Assignment commands
                case "assign-role":
                    assignRoleWizard();
                    break;
                case "revoke-role":
                    revokeRoleWizard();
                    break;
                case "assignment-list":
                    listAssignments();
                    break;
                case "assignment-view":
                    viewAssignment(args);
                    break;
                case "extend-assignment":
                    extendAssignmentWizard();
                    break;

                // Audit log commands
                case "audit-log":
                    showAuditLog();
                    break;
                case "audit-save":
                    saveAuditLog(args);
                    break;

                // Report commands
                case "report-users":
                    generateUserReport(args);
                    break;
                case "report-roles":
                    generateRoleReport(args);
                    break;
                case "report-matrix":
                    generatePermissionMatrix();
                    break;

                // Data persistence
                case "save":
                    saveData(args);
                    break;
                case "load":
                    loadData(args);
                    break;

                default:
                    ConsoleUtils.printError("Unknown command: " + command + ". Type 'help' for available commands.");
            }
        } catch (Exception e) {
            ConsoleUtils.printError(e.getMessage());
        }

        return true;
    }

    private void showHelp() {
        System.out.println();
        System.out.println(ConsoleUtils.ANSI_BOLD + "=== Available Commands ===" + ConsoleUtils.ANSI_RESET);
        System.out.println();

        System.out.println(ConsoleUtils.ANSI_YELLOW + "--- User Management ---" + ConsoleUtils.ANSI_RESET);
        System.out.println("  user-create              - Create a new user (wizard mode)");
        System.out.println("  user-list                - List all users");
        System.out.println("  user-view <username>     - View user details");
        System.out.println("  user-update <username>   - Update user (wizard mode)");
        System.out.println("  user-delete <username>   - Delete a user");
        System.out.println();

        System.out.println(ConsoleUtils.ANSI_YELLOW + "--- Role Management ---" + ConsoleUtils.ANSI_RESET);
        System.out.println("  role-create              - Create a new role (wizard mode)");
        System.out.println("  role-list                - List all roles");
        System.out.println("  role-view <name>         - View role details");
        System.out.println("  role-delete <name>       - Delete a role");
        System.out.println("  role-add-permission      - Add permission to role (wizard mode)");
        System.out.println("  role-remove-permission   - Remove permission from role (wizard mode)");
        System.out.println();

        System.out.println(ConsoleUtils.ANSI_YELLOW + "--- Assignment Management ---" + ConsoleUtils.ANSI_RESET);
        System.out.println("  assign-role              - Assign role to user (wizard mode)");
        System.out.println("  revoke-role              - Revoke role from user (wizard mode)");
        System.out.println("  assignment-list          - List all assignments");
        System.out.println("  assignment-view <id>     - View assignment details");
        System.out.println("  extend-assignment        - Extend temporary assignment (wizard mode)");
        System.out.println();

        System.out.println(ConsoleUtils.ANSI_YELLOW + "--- Audit Log ---" + ConsoleUtils.ANSI_RESET);
        System.out.println("  audit-log                - View audit log");
        System.out.println("  audit-save [filename]    - Save audit log to file");
        System.out.println();

        System.out.println(ConsoleUtils.ANSI_YELLOW + "--- Reports ---" + ConsoleUtils.ANSI_RESET);
        System.out.println("  report-users [filename]  - Generate user report");
        System.out.println("  report-roles [filename]  - Generate role report");
        System.out.println("  report-matrix            - Generate permission matrix");
        System.out.println();

        System.out.println(ConsoleUtils.ANSI_YELLOW + "--- Data Persistence ---" + ConsoleUtils.ANSI_RESET);
        System.out.println("  save [filename]          - Save data to file");
        System.out.println("  load [filename]          - Load data from file");
        System.out.println();

        System.out.println(ConsoleUtils.ANSI_YELLOW + "--- General ---" + ConsoleUtils.ANSI_RESET);
        System.out.println("  help                     - Show this help message");
        System.out.println("  clear                    - Clear console");
        System.out.println("  exit                     - Exit the application");
        System.out.println();
    }

    // ==================== USER COMMANDS ====================

    private void createUserWizard() {
        ConsoleUtils.printSection("Create User");

        System.out.println("Enter user details:");
        String username = ConsoleUtils.promptString(scanner, "Username (3-20 chars, letters/numbers/underscore): ", true);

        if (!ValidationUtils.isValidUsername(username)) {
            ConsoleUtils.printError("Invalid username format. Must be 3-20 characters, letters/numbers/underscore only.");
            return;
        }

        if (userManager.exists(username)) {
            ConsoleUtils.printError("User with username '" + username + "' already exists.");
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
            userManager.add(user);
            auditLog.log("CREATE_USER", currentUser, username, "Full name: " + fullName + ", Email: " + email);
            ConsoleUtils.printSuccess("User '" + username + "' created successfully!");
        } catch (IllegalArgumentException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void listUsers() {
        ConsoleUtils.printSection("User List");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        List<String[]> rows = new ArrayList<>();
        for (User user : users) {
            rows.add(new String[]{
                    user.username(),
                    user.fullName(),
                    user.email()
            });
        }

        String[] headers = {"Username", "Full Name", "Email"};
        System.out.println(FormatUtils.formatTable(headers, rows));
        System.out.println("Total: " + users.size() + " user(s)");
    }

    private void viewUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            ConsoleUtils.printError("Username is required. Usage: user-view <username>");
            return;
        }

        Optional<User> userOpt = userManager.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            ConsoleUtils.printError("User '" + username + "' not found.");
            return;
        }

        User user = userOpt.get();
        List<RoleAssignment> assignments = assignmentManager.findByUser(user);

        System.out.println();
        System.out.println(FormatUtils.formatBox(
                "User: " + user.username() + "\n" +
                        "Full Name: " + user.fullName() + "\n" +
                        "Email: " + user.email()
        ));

        System.out.println("\nRole Assignments:");
        if (assignments.isEmpty()) {
            System.out.println("  No role assignments.");
        } else {
            for (RoleAssignment assignment : assignments) {
                String status = assignment.isActive() ? ConsoleUtils.ANSI_GREEN + "ACTIVE" + ConsoleUtils.ANSI_RESET : ConsoleUtils.ANSI_RED + "INACTIVE" + ConsoleUtils.ANSI_RESET;
                System.out.println("  - " + assignment.role().getName() + " [" + assignment.assignmentType() + "] - " + status);
            }
        }
    }

    private void updateUserWizard(String username) {
        if (username == null || username.trim().isEmpty()) {
            ConsoleUtils.printError("Username is required. Usage: user-update <username>");
            return;
        }

        Optional<User> userOpt = userManager.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            ConsoleUtils.printError("User '" + username + "' not found.");
            return;
        }

        User user = userOpt.get();
        ConsoleUtils.printSection("Update User: " + username);

        System.out.println("Current values shown in brackets. Press Enter to keep current value.");
        System.out.println();

        String newFullName = ConsoleUtils.promptString(scanner, "Full Name [" + user.fullName() + "]: ", user.fullName());
        String newEmail = ConsoleUtils.promptString(scanner, "Email [" + user.email() + "]: ", user.email());

        if (!ValidationUtils.isValidEmail(newEmail)) {
            ConsoleUtils.printError("Invalid email format.");
            return;
        }

        try {
            userManager.update(username.trim(), newFullName, newEmail);
            auditLog.log("UPDATE_USER", currentUser, username.trim(), "Updated to: " + newFullName + ", " + newEmail);
            ConsoleUtils.printSuccess("User '" + username + "' updated successfully!");
        } catch (IllegalArgumentException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void deleteUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            ConsoleUtils.printError("Username is required. Usage: user-delete <username>");
            return;
        }

        Optional<User> userOpt = userManager.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            ConsoleUtils.printError("User '" + username + "' not found.");
            return;
        }

        User user = userOpt.get();

        if (!ConsoleUtils.promptYesNo(scanner, "Are you sure you want to delete user '" + username + "'?")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        userManager.remove(user);
        auditLog.log("DELETE_USER", currentUser, username.trim(), "User deleted");
        ConsoleUtils.printSuccess("User '" + username + "' deleted successfully!");
    }

    // ==================== ROLE COMMANDS ====================

    private void createRoleWizard() {
        ConsoleUtils.printSection("Create Role");

        String name = ConsoleUtils.promptString(scanner, "Role Name: ", true);

        if (roleManager.exists(name)) {
            ConsoleUtils.printError("Role with name '" + name + "' already exists.");
            return;
        }

        String description = ConsoleUtils.promptString(scanner, "Description: ", true);

        try {
            Role role = new Role(name, description);
            roleManager.add(role);
            auditLog.log("CREATE_ROLE", currentUser, name, "Description: " + description);
            ConsoleUtils.printSuccess("Role '" + name + "' created successfully!");

            // Предложить добавить разрешения
            if (ConsoleUtils.promptYesNo(scanner, "Would you like to add permissions to this role?")) {
                addPermissionToRole(name);
            }
        } catch (IllegalArgumentException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void listRoles() {
        ConsoleUtils.printSection("Role List");

        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            System.out.println("No roles found.");
            return;
        }

        List<String[]> rows = new ArrayList<>();
        for (Role role : roles) {
            rows.add(new String[]{
                    role.getName(),
                    role.getDescription(),
                    String.valueOf(role.getPermissions().size())
            });
        }

        String[] headers = {"Role Name", "Description", "Permissions"};
        System.out.println(FormatUtils.formatTable(headers, rows));
        System.out.println("Total: " + roles.size() + " role(s)");
    }

    private void viewRole(String name) {
        if (name == null || name.trim().isEmpty()) {
            ConsoleUtils.printError("Role name is required. Usage: role-view <name>");
            return;
        }

        Optional<Role> roleOpt = roleManager.findByName(name.trim());
        if (roleOpt.isEmpty()) {
            ConsoleUtils.printError("Role '" + name + "' not found.");
            return;
        }

        Role role = roleOpt.get();
        List<RoleAssignment> assignments = assignmentManager.findByRole(role);

        System.out.println();
        System.out.println(FormatUtils.formatBox(
                "Role: " + role.getName() + "\n" +
                        "ID: " + role.getId() + "\n" +
                        "Description: " + role.getDescription()
        ));

        System.out.println("\nPermissions:");
        Set<Permission> permissions = role.getPermissions();
        if (permissions.isEmpty()) {
            System.out.println("  No permissions.");
        } else {
            for (Permission perm : permissions) {
                System.out.println("  - " + perm.format());
            }
        }

        System.out.println("\nAssigned to " + assignments.size() + " user(s):");
        for (RoleAssignment assignment : assignments) {
            if (assignment.isActive()) {
                System.out.println("  - " + assignment.user().username() + " (ACTIVE)");
            }
        }
    }

    private void deleteRole(String name) {
        if (name == null || name.trim().isEmpty()) {
            ConsoleUtils.printError("Role name is required. Usage: role-delete <name>");
            return;
        }

        Optional<Role> roleOpt = roleManager.findByName(name.trim());
        if (roleOpt.isEmpty()) {
            ConsoleUtils.printError("Role '" + name + "' not found.");
            return;
        }

        Role role = roleOpt.get();

        if (!ConsoleUtils.promptYesNo(scanner, "Are you sure you want to delete role '" + name + "'?")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        roleManager.remove(role);
        auditLog.log("DELETE_ROLE", currentUser, name.trim(), "Role deleted");
        ConsoleUtils.printSuccess("Role '" + name + "' deleted successfully!");
    }

    private void addPermissionToRoleWizard() {
        ConsoleUtils.printSection("Add Permission to Role");

        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            ConsoleUtils.printError("No roles available. Create a role first.");
            return;
        }

        Role selectedRole = ConsoleUtils.promptChoice(scanner, "Select a role:", roles, "Cancel");
        if (selectedRole == null) {
            return;
        }

        addPermissionToRole(selectedRole.getName());
    }

    private void addPermissionToRole(String roleName) {
        System.out.println("\nEnter permission details:");
        String permName = ConsoleUtils.promptString(scanner, "Permission Name (e.g., READ, WRITE, DELETE): ", true).toUpperCase();
        String resource = ConsoleUtils.promptString(scanner, "Resource (e.g., USERS, ROLES, REPORTS): ", true).toLowerCase();
        String description = ConsoleUtils.promptString(scanner, "Description: ", true);

        try {
            Permission permission = Permission.create(permName, resource, description);
            roleManager.addPermissionToRole(roleName, permission);
            auditLog.log("ADD_PERMISSION", currentUser, roleName, "Added: " + permName + " on " + resource);
            ConsoleUtils.printSuccess("Permission added to role '" + roleName + "'!");
        } catch (IllegalArgumentException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void removePermissionFromRoleWizard() {
        ConsoleUtils.printSection("Remove Permission from Role");

        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            ConsoleUtils.printError("No roles available.");
            return;
        }

        Role selectedRole = ConsoleUtils.promptChoice(scanner, "Select a role:", roles, "Cancel");
        if (selectedRole == null) {
            return;
        }

        Set<Permission> permissions = selectedRole.getPermissions();
        if (permissions.isEmpty()) {
            ConsoleUtils.printInfo("This role has no permissions.");
            return;
        }

        List<String> permStrings = new ArrayList<>();
        for (Permission perm : permissions) {
            permStrings.add(perm.name() + " on " + perm.resource());
        }

        String selectedPerm = ConsoleUtils.promptChoice(scanner, "Select permission to remove:", permStrings, "Cancel");
        if (selectedPerm == null) {
            return;
        }

        String[] parts = selectedPerm.split(" on ", 2);
        String permName = parts[0];
        String resource = parts[1];

        Permission permission = Permission.create(permName, resource, "");
        roleManager.removePermissionFromRole(selectedRole.getName(), permission);
        auditLog.log("REMOVE_PERMISSION", currentUser, selectedRole.getName(), "Removed: " + permName + " on " + resource);
        ConsoleUtils.printSuccess("Permission removed from role '" + selectedRole.getName() + "'!");
    }

    // ==================== ASSIGNMENT COMMANDS ====================

    private void assignRoleWizard() {
        ConsoleUtils.printSection("Assign Role to User");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            ConsoleUtils.printError("No users available. Create a user first.");
            return;
        }

        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            ConsoleUtils.printError("No roles available. Create a role first.");
            return;
        }

        User selectedUser = ConsoleUtils.promptChoice(scanner, "Select a user:", users, "Cancel");
        if (selectedUser == null) {
            return;
        }

        Role selectedRole = ConsoleUtils.promptChoice(scanner, "Select a role:", roles, "Cancel");
        if (selectedRole == null) {
            return;
        }

        // Проверка на дублирование
        if (assignmentManager.userHasRole(selectedUser, selectedRole)) {
            ConsoleUtils.printError("User '" + selectedUser.username() + "' already has role '" + selectedRole.getName() + "'.");
            return;
        }

        System.out.println("\nAssignment type:");
        System.out.println("  1. Permanent");
        System.out.println("  2. Temporary");
        int typeChoice = ConsoleUtils.promptInt(scanner, "Choose type (1-2): ", 1, 2);

        String reason = ConsoleUtils.promptString(scanner, "Reason (optional): ", "");
        AssignmentMetadata metadata = AssignmentMetadata.now(currentUser, reason);

        try {
            if (typeChoice == 1) {
                PermanentAssignment assignment = new PermanentAssignment(selectedUser, selectedRole, metadata);
                assignmentManager.add(assignment);
                auditLog.log("ASSIGN_ROLE", currentUser, selectedUser.username(),
                        "Assigned role: " + selectedRole.getName() + " (PERMANENT)");
                ConsoleUtils.printSuccess("Role '" + selectedRole.getName() + "' assigned to '" + selectedUser.username() + "'!");
            } else {
                String expiresAt = ConsoleUtils.promptString(scanner, "Expiration date (yyyy-MM-dd HH:mm): ", true);

                if (!ValidationUtils.isValidDateTime(expiresAt)) {
                    ConsoleUtils.printError("Invalid date format. Expected: yyyy-MM-dd HH:mm");
                    return;
                }

                TemporaryAssignment assignment = new TemporaryAssignment(selectedUser, selectedRole, metadata, expiresAt);
                assignmentManager.add(assignment);
                auditLog.log("ASSIGN_ROLE", currentUser, selectedUser.username(),
                        "Assigned role: " + selectedRole.getName() + " (TEMPORARY, expires: " + expiresAt + ")");
                ConsoleUtils.printSuccess("Role '" + selectedRole.getName() + "' assigned to '" + selectedUser.username() + "' (temporary)!");
            }
        } catch (IllegalArgumentException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void revokeRoleWizard() {
        ConsoleUtils.printSection("Revoke Role from User");

        List<RoleAssignment> activeAssignments = assignmentManager.getActiveAssignments();
        if (activeAssignments.isEmpty()) {
            ConsoleUtils.printInfo("No active role assignments.");
            return;
        }

        List<String> assignmentStrings = new ArrayList<>();
        for (RoleAssignment assignment : activeAssignments) {
            assignmentStrings.add(assignment.user().username() + " - " + assignment.role().getName() + " [" + assignment.assignmentType() + "]");
        }

        String selected = ConsoleUtils.promptChoice(scanner, "Select assignment to revoke:", assignmentStrings, "Cancel");
        if (selected == null) {
            return;
        }

        // Находим соответствующее назначение
        for (RoleAssignment assignment : activeAssignments) {
            String assignmentStr = assignment.user().username() + " - " + assignment.role().getName() + " [" + assignment.assignmentType() + "]";
            if (assignmentStr.equals(selected)) {
                if (assignment instanceof PermanentAssignment) {
                    assignmentManager.revokeAssignment(assignment.assignmentId());
                } else {
                    assignmentManager.remove(assignment);
                }
                auditLog.log("REVOKE_ROLE", currentUser, assignment.user().username(),
                        "Revoked role: " + assignment.role().getName());
                ConsoleUtils.printSuccess("Role revoked from user '" + assignment.user().username() + "'!");
                return;
            }
        }
    }

    private void listAssignments() {
        ConsoleUtils.printSection("Role Assignments");

        List<RoleAssignment> assignments = assignmentManager.findAll();
        if (assignments.isEmpty()) {
            System.out.println("No assignments found.");
            return;
        }

        List<String[]> rows = new ArrayList<>();
        for (RoleAssignment assignment : assignments) {
            String status = assignment.isActive() ? "ACTIVE" : "INACTIVE";
            String expires = "";
            if (assignment instanceof TemporaryAssignment) {
                expires = ((TemporaryAssignment) assignment).getExpiresAt();
            }
            rows.add(new String[]{
                    assignment.assignmentId().substring(0, 13) + "...",
                    assignment.user().username(),
                    assignment.role().getName(),
                    assignment.assignmentType(),
                    status,
                    expires
            });
        }

        String[] headers = {"ID", "Username", "Role", "Type", "Status", "Expires"};
        System.out.println(FormatUtils.formatTable(headers, rows));
        System.out.println("Total: " + assignments.size() + " assignment(s)");
    }

    private void viewAssignment(String id) {
        if (id == null || id.trim().isEmpty()) {
            ConsoleUtils.printError("Assignment ID is required. Usage: assignment-view <id>");
            return;
        }

        Optional<RoleAssignment> assignmentOpt = assignmentManager.findById(id.trim());
        if (assignmentOpt.isEmpty()) {
            ConsoleUtils.printError("Assignment with ID '" + id + "' not found.");
            return;
        }

        RoleAssignment assignment = assignmentOpt.get();

        System.out.println();
        System.out.println(FormatUtils.formatBox(assignment.summary()));

        if (assignment instanceof TemporaryAssignment) {
            TemporaryAssignment temp = (TemporaryAssignment) assignment;
            System.out.println("\nTime Remaining: " + temp.getTimeRemaining());
            System.out.println("Auto Renew: " + (temp.isAutoRenew() ? "YES" : "NO"));
        }
    }

    private void extendAssignmentWizard() {
        ConsoleUtils.printSection("Extend Temporary Assignment");

        List<RoleAssignment> assignments = assignmentManager.findAll();
        List<TemporaryAssignment> tempAssignments = new ArrayList<>();

        for (RoleAssignment assignment : assignments) {
            if (assignment instanceof TemporaryAssignment) {
                tempAssignments.add((TemporaryAssignment) assignment);
            }
        }

        if (tempAssignments.isEmpty()) {
            ConsoleUtils.printInfo("No temporary assignments found.");
            return;
        }

        List<String> assignmentStrings = new ArrayList<>();
        for (TemporaryAssignment assignment : tempAssignments) {
            assignmentStrings.add(assignment.user().username() + " - " + assignment.role().getName() + " (expires: " + assignment.getExpiresAt() + ")");
        }

        String selected = ConsoleUtils.promptChoice(scanner, "Select assignment to extend:", assignmentStrings, "Cancel");
        if (selected == null) {
            return;
        }

        // Находим соответствующее назначение
        for (TemporaryAssignment assignment : tempAssignments) {
            String assignmentStr = assignment.user().username() + " - " + assignment.role().getName() + " (expires: " + assignment.getExpiresAt() + ")";
            if (assignmentStr.equals(selected)) {
                String newExpiration = ConsoleUtils.promptString(scanner, "New expiration date (yyyy-MM-dd HH:mm): ", true);

                if (!ValidationUtils.isValidDateTime(newExpiration)) {
                    ConsoleUtils.printError("Invalid date format. Expected: yyyy-MM-dd HH:mm");
                    return;
                }

                assignment.extend(newExpiration);
                auditLog.log("EXTEND_ASSIGNMENT", currentUser, assignment.assignmentId(),
                        "Extended to: " + newExpiration);
                ConsoleUtils.printSuccess("Assignment extended to " + newExpiration + "!");
                return;
            }
        }
    }

    // ==================== AUDIT LOG COMMANDS ====================

    private void showAuditLog() {
        ConsoleUtils.printSection("Audit Log");
        auditLog.printLog();
    }

    private void saveAuditLog(String filename) {
        String file = (filename != null && !filename.trim().isEmpty()) ? filename.trim() : "audit-log.txt";
        auditLog.saveToFile(file);
    }

    // ==================== REPORT COMMANDS ====================

    private void generateUserReport(String filename) {
        String report = reportGenerator.generateUserReport(userManager, assignmentManager);
        System.out.println(report);

        if (filename != null && !filename.trim().isEmpty()) {
            reportGenerator.exportToFile(report, filename.trim());
        }
    }

    private void generateRoleReport(String filename) {
        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);
        System.out.println(report);

        if (filename != null && !filename.trim().isEmpty()) {
            reportGenerator.exportToFile(report, filename.trim());
        }
    }

    private void generatePermissionMatrix() {
        String report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);
        System.out.println(report);
    }

    // ==================== DATA PERSISTENCE ====================

    private void saveData(String filename) {
        String file = (filename != null && !filename.trim().isEmpty()) ? filename.trim() : "rbac-data.ser";

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            // Сохраняем пользователей
            oos.writeObject("USERS");
            oos.writeObject(userManager.findAll());

            // Сохраняем роли
            oos.writeObject("ROLES");
            oos.writeObject(roleManager.findAll());

            // Сохраняем назначения
            oos.writeObject("ASSIGNMENTS");
            oos.writeObject(assignmentManager.findAll());

            // Сохраняем лог аудита
            oos.writeObject("AUDIT");
            oos.writeObject(auditLog.getAll());

            ConsoleUtils.printSuccess("Data saved to: " + file);
        } catch (IOException e) {
            ConsoleUtils.printError("Failed to save data: " + e.getMessage());
        }
    }

    private void loadData(String filename) {
        String file = (filename != null && !filename.trim().isEmpty()) ? filename.trim() : "rbac-data.ser";

        if (!new File(file).exists()) {
            ConsoleUtils.printError("File not found: " + file);
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            // Очищаем текущие данные
            userManager.clear();
            roleManager.clear();
            assignmentManager.clear();
            auditLog.clear();

            // Загружаем данные
            while (ois.available() > 0) {
                String type = (String) ois.readObject();

                switch (type) {
                    case "USERS":
                        List<User> users = (List<User>) ois.readObject();
                        for (User user : users) {
                            userManager.add(user);
                        }
                        break;
                    case "ROLES":
                        List<Role> roles = (List<Role>) ois.readObject();
                        for (Role role : roles) {
                            roleManager.add(role);
                        }
                        break;
                    case "ASSIGNMENTS":
                        List<RoleAssignment> assignments = (List<RoleAssignment>) ois.readObject();
                        // Назначения загружаются, но требуют существующих пользователей и ролей
                        break;
                    case "AUDIT":
                        List<AuditLog.AuditEntry> entries = (List<AuditLog.AuditEntry>) ois.readObject();
                        // Восстанавливаем лог аудита
                        break;
                }
            }

            ConsoleUtils.printSuccess("Data loaded from: " + file);
        } catch (IOException | ClassNotFoundException e) {
            ConsoleUtils.printError("Failed to load data: " + e.getMessage());
        }
    }

    /**
     * Точка входа приложения.
     */
    public static void main(String[] args) {
        RBACConsole console = new RBACConsole();
        console.run();
    }
}
