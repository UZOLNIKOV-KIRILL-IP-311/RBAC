package org.example.report;

import org.example.manager.AssignmentManager;
import org.example.manager.RoleManager;
import org.example.manager.UserManager;
import org.example.rbac.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Генератор отчётов для RBAC системы.
 */
public class ReportGenerator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReportGenerator() {
    }

    /**
     * Генерирует отчёт по всем пользователям с их ролями.
     *
     * @param userManager менеджер пользователей
     * @param assignmentManager менеджер назначений
     * @return отформатированный отчёт
     */
    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();

        sb.append("================================================================================\n");
        sb.append("                         USER REPORT\n");
        sb.append("                    Generated: ").append(LocalDateTime.now().format(DATE_TIME_FORMATTER)).append("\n");
        sb.append("================================================================================\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("No users found.\n");
            return sb.toString();
        }

        sb.append(String.format("%-20s | %-25s | %-30s | %-15s%n",
                "Username", "Full Name", "Email", "Roles Count"));
        sb.append("--------------------------------------------------------------------------------\n");

        for (User user : users) {
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            int rolesCount = (int) assignments.stream().filter(RoleAssignment::isActive).count();

            String fullName = truncate(user.fullName(), 25);
            String email = truncate(user.email(), 30);

            sb.append(String.format("%-20s | %-25s | %-30s | %-15d%n",
                    truncate(user.username(), 20),
                    fullName,
                    email,
                    rolesCount));
        }

        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("Total users: ").append(users.size()).append("\n");

        // Детализация по ролям для каждого пользователя
        sb.append("\n=== DETAILED ROLE ASSIGNMENTS ===\n\n");

        for (User user : users) {
            sb.append("User: ").append(user.username()).append("\n");
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);

            if (assignments.isEmpty()) {
                sb.append("  No role assignments.\n");
            } else {
                for (RoleAssignment assignment : assignments) {
                    String status = assignment.isActive() ? "ACTIVE" : "INACTIVE";
                    sb.append(String.format("  - %s [%s] (%s) - %s%n",
                            assignment.role().getName(),
                            assignment.assignmentType(),
                            status,
                            assignment.metadata().assignedBy()));
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Генерирует отчёт по ролям с количеством пользователей.
     *
     * @param roleManager менеджер ролей
     * @param assignmentManager менеджер назначений
     * @return отформатированный отчёт
     */
    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();

        sb.append("================================================================================\n");
        sb.append("                         ROLE REPORT\n");
        sb.append("                    Generated: ").append(LocalDateTime.now().format(DATE_TIME_FORMATTER)).append("\n");
        sb.append("================================================================================\n\n");

        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            sb.append("No roles found.\n");
            return sb.toString();
        }

        sb.append(String.format("%-25s | %-15s | %-20s | %-15s%n",
                "Role Name", "ID", "Permissions", "Active Users"));
        sb.append("--------------------------------------------------------------------------------\n");

        for (Role role : roles) {
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);
            long activeUsers = assignments.stream().filter(RoleAssignment::isActive).count();

            sb.append(String.format("%-25s | %-15s | %-20d | %-15d%n",
                    truncate(role.getName(), 25),
                    truncate(role.getId(), 15),
                    role.getPermissions().size(),
                    activeUsers));
        }

        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("Total roles: ").append(roles.size()).append("\n");

        // Детализация по разрешениям для каждой роли
        sb.append("\n=== DETAILED PERMISSIONS ===\n\n");

        for (Role role : roles) {
            sb.append("Role: ").append(role.getName()).append("\n");
            sb.append("Description: ").append(role.getDescription()).append("\n");
            Set<Permission> permissions = role.getPermissions();

            if (permissions.isEmpty()) {
                sb.append("  No permissions.\n");
            } else {
                sb.append("  Permissions:\n");
                for (Permission perm : permissions) {
                    sb.append(String.format("    - %s on %s: %s%n",
                            perm.name(),
                            perm.resource(),
                            truncate(perm.description(), 40)));
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Генерирует матрицу прав (пользователи × ресурсы).
     *
     * @param userManager менеджер пользователей
     * @param assignmentManager менеджер назначений
     * @return отформатированная матрица прав
     */
    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();

        sb.append("================================================================================\n");
        sb.append("                      PERMISSION MATRIX\n");
        sb.append("                    Generated: ").append(LocalDateTime.now().format(DATE_TIME_FORMATTER)).append("\n");
        sb.append("================================================================================\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("No users found.\n");
            return sb.toString();
        }

        // Собираем все уникальные ресурсы
        Set<String> allResources = new TreeSet<>();
        Map<String, Set<String>> userPermissions = new HashMap<>();

        for (User user : users) {
            Set<Permission> permissions = assignmentManager.getUserPermissions(user);
            Set<String> userPerms = new HashSet<>();

            for (Permission perm : permissions) {
                allResources.add(perm.resource());
                userPerms.add(perm.name() + ":" + perm.resource());
            }

            userPermissions.put(user.username(), userPerms);
        }

        List<String> resources = new ArrayList<>(allResources);
        List<String> permissionTypes = Arrays.asList("READ", "WRITE", "DELETE", "ADMIN");

        // Заголовок таблицы
        sb.append(String.format("%-20s | ", "Username"));

        for (String resource : resources) {
            for (String permType : permissionTypes) {
                sb.append(String.format("%-6s | ", permType.substring(0, Math.min(4, permType.length()))));
            }
        }
        sb.append("\n");

        // Разделитель
        sb.append(String.format("%-20s-+", "-".repeat(20)));
        for (String resource : resources) {
            for (String permType : permissionTypes) {
                sb.append("-------+");
            }
        }
        sb.append("\n");

        // Строки пользователей
        for (User user : users) {
            Set<String> perms = userPermissions.get(user.username());
            sb.append(String.format("%-20s | ", truncate(user.username(), 20)));

            for (String resource : resources) {
                for (String permType : permissionTypes) {
                    String permKey = permType + ":" + resource;
                    if (perms.contains(permKey)) {
                        sb.append(String.format("%-6s | ", "Y"));
                    } else {
                        sb.append(String.format("%-6s | ", "-"));
                    }
                }
            }
            sb.append("\n");
        }

        sb.append("\nLegend: Y = Has permission, - = No permission\n");
        sb.append("Total users: ").append(users.size()).append(", Total resources: ").append(resources.size()).append("\n");

        return sb.toString();
    }

    /**
     * Сохраняет отчёт в файл.
     *
     * @param report текст отчёта
     * @param filename имя файла
     */
    public void exportToFile(String report, String filename) {
        if (report == null) {
            throw new IllegalArgumentException("Report cannot be null");
        }
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(report);
            System.out.println("Report exported to: " + filename);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export report to file: " + filename, e);
        }
    }

    /**
     * Обрезает строку до указанной длины с добавлением "...".
     *
     * @param text строка
     * @param maxLength максимальная длина
     * @return обрезанная строка
     */
    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
