package org.example.report;

import org.example.manager.AssignmentManager;
import org.example.manager.RoleManager;
import org.example.manager.UserManager;
import org.example.rbac.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private ReportGenerator reportGenerator;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
        reportGenerator = new ReportGenerator();

        // Setup test data
        User user1 = User.create("john_doe", "John Doe", "john@example.com");
        User user2 = User.create("jane_smith", "Jane Smith", "jane@example.com");
        userManager.add(user1);
        userManager.add(user2);

        Role adminRole = new Role("Administrator", "Full access");
        adminRole.addPermission(Permission.create("READ", "USERS", "Can read users"));
        adminRole.addPermission(Permission.create("WRITE", "USERS", "Can write users"));
        Role viewerRole = new Role("Viewer", "Read-only access");
        viewerRole.addPermission(Permission.create("READ", "USERS", "Can read users"));
        roleManager.add(adminRole);
        roleManager.add(viewerRole);

        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Test setup");
        PermanentAssignment assignment1 = new PermanentAssignment(user1, adminRole, metadata);
        PermanentAssignment assignment2 = new PermanentAssignment(user2, viewerRole, metadata);
        assignmentManager.add(assignment1);
        assignmentManager.add(assignment2);
    }

    @Test
    void testGenerateUserReport() {
        String report = reportGenerator.generateUserReport(userManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("USER REPORT"));
        assertTrue(report.contains("john_doe"));
        assertTrue(report.contains("jane_smith"));
        assertTrue(report.contains("john@example.com"));
        assertTrue(report.contains("Administrator"));
        assertTrue(report.contains("Viewer"));
    }

    @Test
    void testGenerateUserReportEmpty() {
        UserManager emptyManager = new UserManager();
        AssignmentManager emptyAssignmentManager = new AssignmentManager(emptyManager, roleManager);

        String report = reportGenerator.generateUserReport(emptyManager, emptyAssignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("No users found"));
    }

    @Test
    void testGenerateRoleReport() {
        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("ROLE REPORT"));
        assertTrue(report.contains("Administrator"));
        assertTrue(report.contains("Viewer"));
        assertTrue(report.contains("Permissions"));
    }

    @Test
    void testGenerateRoleReportEmpty() {
        RoleManager emptyRoleManager = new RoleManager();

        String report = reportGenerator.generateRoleReport(emptyRoleManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("No roles found"));
    }

    @Test
    void testGeneratePermissionMatrix() {
        String matrix = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        assertNotNull(matrix);
        assertTrue(matrix.contains("PERMISSION MATRIX"));
        assertTrue(matrix.contains("john_doe"));
        assertTrue(matrix.contains("jane_smith"));
        // Проверяем, что матрица содержит заголовки или данные о правах
        assertTrue(matrix.contains("Username") || matrix.contains("READ") || matrix.contains("WRITE"));
    }

    @Test
    void testGeneratePermissionMatrixEmpty() {
        UserManager emptyManager = new UserManager();
        AssignmentManager emptyAssignmentManager = new AssignmentManager(emptyManager, roleManager);

        String matrix = reportGenerator.generatePermissionMatrix(emptyManager, emptyAssignmentManager);

        assertNotNull(matrix);
        assertTrue(matrix.contains("No users found"));
    }

    @Test
    void testExportToFile() throws Exception {
        String testFile = "test-report.txt";
        String report = "Test Report Content";

        reportGenerator.exportToFile(report, testFile);

        File file = new File(testFile);
        assertTrue(file.exists());

        String content = new String(Files.readAllBytes(file.toPath()));
        assertEquals(report, content);

        // Cleanup
        file.delete();
    }

    @Test
    void testExportToFileNullReport() {
        assertThrows(IllegalArgumentException.class, () ->
                reportGenerator.exportToFile(null, "test.txt"));
    }

    @Test
    void testExportToFileEmptyFilename() {
        assertThrows(IllegalArgumentException.class, () ->
                reportGenerator.exportToFile("report", ""));
        assertThrows(IllegalArgumentException.class, () ->
                reportGenerator.exportToFile("report", null));
    }
}
