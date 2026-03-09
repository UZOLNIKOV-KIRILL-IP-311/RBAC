package org.example.console;

import org.example.manager.AssignmentManager;
import org.example.manager.RoleManager;
import org.example.manager.UserManager;
import org.example.rbac.*;

/**
 * Главная система RBAC, содержащая все менеджеры.
 */
public class RBACSystem {

    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.currentUser = "system";
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        this.currentUser = username.trim();
    }

    /**
     * Инициализация системы: создание предустановленных данных.
     */
    public void initialize() {
        // Создаём права доступа по умолчанию
        Permission readUsers = Permission.create("READ", "USERS", "Can view user list");
        Permission writeUsers = Permission.create("WRITE", "USERS", "Can create and edit users");
        Permission deleteUsers = Permission.create("DELETE", "USERS", "Can delete users");

        Permission readRoles = Permission.create("READ", "ROLES", "Can view roles");
        Permission writeRoles = Permission.create("WRITE", "ROLES", "Can create and edit roles");
        Permission deleteRoles = Permission.create("DELETE", "ROLES", "Can delete roles");

        Permission readReports = Permission.create("READ", "REPORTS", "Can view reports");
        Permission writeReports = Permission.create("WRITE", "REPORTS", "Can create reports");

        Permission adminSystem = Permission.create("ADMIN", "SYSTEM", "Full system access");

        // Создаём роли
        Role adminRole = new Role("Admin", "System administrator with full access");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        adminRole.addPermission(readRoles);
        adminRole.addPermission(writeRoles);
        adminRole.addPermission(deleteRoles);
        adminRole.addPermission(readReports);
        adminRole.addPermission(writeReports);
        adminRole.addPermission(adminSystem);

        Role managerRole = new Role("Manager", "Manager with user management capabilities");
        managerRole.addPermission(readUsers);
        managerRole.addPermission(writeUsers);
        managerRole.addPermission(readReports);
        managerRole.addPermission(writeReports);

        Role viewerRole = new Role("Viewer", "Read-only access to view data");
        viewerRole.addPermission(readUsers);
        viewerRole.addPermission(readRoles);
        viewerRole.addPermission(readReports);

        roleManager.add(adminRole);
        roleManager.add(managerRole);
        roleManager.add(viewerRole);

        // Создаём администратора
        User admin = User.create("admin", "System Administrator", "admin@company.com");
        userManager.add(admin);

        // Назначаем роль Admin администратору
        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Initial system setup");
        PermanentAssignment adminAssignment = new PermanentAssignment(admin, adminRole, metadata);
        assignmentManager.add(adminAssignment);

        // Создаём тестовых пользователей
        User manager = User.create("manager", "John Manager", "manager@company.com");
        userManager.add(manager);
        PermanentAssignment managerAssignment = new PermanentAssignment(manager, managerRole, metadata);
        assignmentManager.add(managerAssignment);

        User viewer = User.create("viewer", "Jane Viewer", "viewer@company.com");
        userManager.add(viewer);
        PermanentAssignment viewerAssignment = new PermanentAssignment(viewer, viewerRole, metadata);
        assignmentManager.add(viewerAssignment);
    }

    /**
     * Генерация статистики системы.
     *
     * @return отформатированная строка статистики
     */
    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();

        sb.append("==============================================\n");
        sb.append("           RBAC SYSTEM STATISTICS\n");
        sb.append("==============================================\n\n");

        int userCount = userManager.count();
        int roleCount = roleManager.count();
        int totalAssignments = assignmentManager.count();
        int activeAssignments = assignmentManager.getActiveAssignments().size();
        int expiredAssignments = assignmentManager.getExpiredAssignments().size();

        sb.append("Users: ").append(userCount).append("\n");
        sb.append("Roles: ").append(roleCount).append("\n");
        sb.append("Assignments:\n");
        sb.append("  - Total: ").append(totalAssignments).append("\n");
        sb.append("  - Active: ").append(activeAssignments).append("\n");
        sb.append("  - Expired: ").append(expiredAssignments).append("\n");

        // Среднее количество ролей на пользователя
        if (userCount > 0) {
            double avgRoles = (double) activeAssignments / userCount;
            sb.append(String.format("\nAverage roles per user: %.2f\n", avgRoles));
        }

        // Топ-3 самых популярных ролей
        sb.append("\nTop 3 Most Popular Roles:\n");
        roleManager.findAll().stream()
                .sorted((r1, r2) -> {
                    int count1 = assignmentManager.findByRole(r1).size();
                    int count2 = assignmentManager.findByRole(r2).size();
                    return Integer.compare(count2, count1);
                })
                .limit(3)
                .forEach(role -> {
                    int count = assignmentManager.findByRole(role).size();
                    sb.append("  - ").append(role.getName()).append(": ").append(count).append(" assignments\n");
                });

        sb.append("\n==============================================\n");
        sb.append("Current user: ").append(currentUser).append("\n");
        sb.append("==============================================\n");

        return sb.toString();
    }
}
