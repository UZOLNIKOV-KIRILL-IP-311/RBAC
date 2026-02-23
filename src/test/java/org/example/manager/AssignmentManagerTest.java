package org.example.manager;

import org.example.rbac.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentManagerTest {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);

        // Создаем тестовых пользователей
        userManager.add(User.create("john_doe", "John Doe", "john@example.com"));
        userManager.add(User.create("jane_smith", "Jane Smith", "jane@example.com"));

        // Создаем тестовые роли
        Role adminRole = new Role("Admin", "Administrator role");
        adminRole.addPermission(Permission.create("READ", "USERS", "Read users"));
        adminRole.addPermission(Permission.create("WRITE", "USERS", "Write users"));
        roleManager.add(adminRole);

        Role viewerRole = new Role("Viewer", "Viewer role");
        viewerRole.addPermission(Permission.create("READ", "USERS", "Read users"));
        roleManager.add(viewerRole);
    }

    @Test
    void testAddAssignment() {
        User user = userManager.findByUsername("john_doe").get();
        Role role = roleManager.findByName("Admin").get();
        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Test assignment");

        RoleAssignment assignment = new PermanentAssignment(user, role, metadata);
        assignmentManager.add(assignment);

        assertEquals(1, assignmentManager.count());
        assertTrue(assignmentManager.userHasRole(user, role));
    }

    @Test
    void testAddAssignmentWithNonExistentUser() {
        User unknownUser = User.create("unknown", "Unknown User", "unknown@example.com");
        Role role = roleManager.findByName("Admin").get();
        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Test");

        RoleAssignment assignment = new PermanentAssignment(unknownUser, role, metadata);
        assertThrows(IllegalArgumentException.class, () -> assignmentManager.add(assignment));
    }

    @Test
    void testAddAssignmentWithNonExistentRole() {
        User user = userManager.findByUsername("john_doe").get();
        Role unknownRole = new Role("Unknown", "Unknown role");
        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Test");

        RoleAssignment assignment = new PermanentAssignment(user, unknownRole, metadata);
        assertThrows(IllegalArgumentException.class, () -> assignmentManager.add(assignment));
    }

    @Test
    void testAddDuplicateAssignment() {
        User user = userManager.findByUsername("john_doe").get();
        Role role = roleManager.findByName("Admin").get();
        AssignmentMetadata metadata1 = AssignmentMetadata.now("system", "Test 1");
        AssignmentMetadata metadata2 = AssignmentMetadata.now("system", "Test 2");

        RoleAssignment assignment1 = new PermanentAssignment(user, role, metadata1);
        RoleAssignment assignment2 = new PermanentAssignment(user, role, metadata2);

        assignmentManager.add(assignment1);
        assertThrows(IllegalArgumentException.class, () -> assignmentManager.add(assignment2));
    }

    @Test
    void testRemoveAssignment() {
        User user = userManager.findByUsername("john_doe").get();
        Role role = roleManager.findByName("Admin").get();
        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Test");

        RoleAssignment assignment = new PermanentAssignment(user, role, metadata);
        assignmentManager.add(assignment);

        assertTrue(assignmentManager.remove(assignment));
        assertEquals(0, assignmentManager.count());
    }

    @Test
    void testFindById() {
        User user = userManager.findByUsername("john_doe").get();
        Role role = roleManager.findByName("Admin").get();
        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Test");

        RoleAssignment assignment = new PermanentAssignment(user, role, metadata);
        assignmentManager.add(assignment);

        Optional<RoleAssignment> found = assignmentManager.findById(assignment.assignmentId());
        assertTrue(found.isPresent());
        assertEquals(assignment.assignmentId(), found.get().assignmentId());
    }

    @Test
    void testFindAll() {
        User user1 = userManager.findByUsername("john_doe").get();
        User user2 = userManager.findByUsername("jane_smith").get();
        Role adminRole = roleManager.findByName("Admin").get();
        Role viewerRole = roleManager.findByName("Viewer").get();

        AssignmentMetadata metadata1 = AssignmentMetadata.now("system", "Test 1");
        AssignmentMetadata metadata2 = AssignmentMetadata.now("system", "Test 2");

        assignmentManager.add(new PermanentAssignment(user1, adminRole, metadata1));
        assignmentManager.add(new PermanentAssignment(user2, viewerRole, metadata2));

        List<RoleAssignment> assignments = assignmentManager.findAll();
        assertEquals(2, assignments.size());
    }

    @Test
    void testFindByUser() {
        User user = userManager.findByUsername("john_doe").get();
        Role adminRole = roleManager.findByName("Admin").get();
        Role viewerRole = roleManager.findByName("Viewer").get();

        AssignmentMetadata metadata1 = AssignmentMetadata.now("system", "Test 1");
        AssignmentMetadata metadata2 = AssignmentMetadata.now("system", "Test 2");

        assignmentManager.add(new PermanentAssignment(user, adminRole, metadata1));
        assignmentManager.add(new PermanentAssignment(user, viewerRole, metadata2));

        List<RoleAssignment> userAssignments = assignmentManager.findByUser(user);
        assertEquals(2, userAssignments.size());
    }

    @Test
    void testFindByRole() {
        User user1 = userManager.findByUsername("john_doe").get();
        User user2 = userManager.findByUsername("jane_smith").get();
        Role adminRole = roleManager.findByName("Admin").get();

        AssignmentMetadata metadata1 = AssignmentMetadata.now("system", "Test 1");
        AssignmentMetadata metadata2 = AssignmentMetadata.now("system", "Test 2");

        assignmentManager.add(new PermanentAssignment(user1, adminRole, metadata1));
        assignmentManager.add(new PermanentAssignment(user2, adminRole, metadata2));

        List<RoleAssignment> roleAssignments = assignmentManager.findByRole(adminRole);
        assertEquals(2, roleAssignments.size());
    }

    @Test
    void testFindByFilter() {
        User user1 = userManager.findByUsername("john_doe").get();
        User user2 = userManager.findByUsername("jane_smith").get();
        Role adminRole = roleManager.findByName("Admin").get();

        AssignmentMetadata metadata1 = AssignmentMetadata.create("admin", "2026-01-15 10:00", "Test 1");
        AssignmentMetadata metadata2 = AssignmentMetadata.create("system", "2026-02-20 14:30", "Test 2");

        assignmentManager.add(new PermanentAssignment(user1, adminRole, metadata1));
        assignmentManager.add(new PermanentAssignment(user2, adminRole, metadata2));

        AssignmentFilter filter = AssignmentFilters.assignedBy("admin");
        List<RoleAssignment> filtered = assignmentManager.findByFilter(filter);

        assertEquals(1, filtered.size());
    }

    @Test
    void testGetActiveAssignments() {
        User user = userManager.findByUsername("john_doe").get();
        Role role = roleManager.findByName("Admin").get();
        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Test");

        PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
        assignmentManager.add(assignment);
        assignment.revoke();

        List<RoleAssignment> active = assignmentManager.getActiveAssignments();
        assertEquals(0, active.size());
    }

    @Test
    void testGetExpiredAssignments() {
        User user = userManager.findByUsername("john_doe").get();
        Role role = roleManager.findByName("Admin").get();
        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Test");

        // Создаем просроченное временное назначение
        TemporaryAssignment expiredAssignment = new TemporaryAssignment(user, role, metadata, "2020-01-01 00:00");
        assignmentManager.add(expiredAssignment);

        List<RoleAssignment> expired = assignmentManager.getExpiredAssignments();
        assertEquals(1, expired.size());
    }

    @Test
    void testUserHasRole() {
        User user = userManager.findByUsername("john_doe").get();
        Role adminRole = roleManager.findByName("Admin").get();
        Role viewerRole = roleManager.findByName("Viewer").get();

        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Test");
        assignmentManager.add(new PermanentAssignment(user, adminRole, metadata));

        assertTrue(assignmentManager.userHasRole(user, adminRole));
        assertFalse(assignmentManager.userHasRole(user, viewerRole));
    }

    @Test
    void testUserHasPermission() {
        User user = userManager.findByUsername("john_doe").get();
        Role adminRole = roleManager.findByName("Admin").get();

        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Test");
        assignmentManager.add(new PermanentAssignment(user, adminRole, metadata));

        assertTrue(assignmentManager.userHasPermission(user, "READ", "USERS"));
        assertTrue(assignmentManager.userHasPermission(user, "WRITE", "USERS"));
        assertFalse(assignmentManager.userHasPermission(user, "DELETE", "USERS"));
    }

    @Test
    void testGetUserPermissions() {
        User user = userManager.findByUsername("john_doe").get();
        Role adminRole = roleManager.findByName("Admin").get();

        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Test");
        assignmentManager.add(new PermanentAssignment(user, adminRole, metadata));

        Set<Permission> permissions = assignmentManager.getUserPermissions(user);
        assertEquals(2, permissions.size());
    }

    @Test
    void testRevokeAssignment() {
        User user = userManager.findByUsername("john_doe").get();
        Role role = roleManager.findByName("Admin").get();
        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Test");

        PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
        assignmentManager.add(assignment);

        assignmentManager.revokeAssignment(assignment.assignmentId());

        assertFalse(assignment.isActive());
    }

    @Test
    void testRevokeNonExistentAssignment() {
        assertThrows(IllegalArgumentException.class,
                () -> assignmentManager.revokeAssignment("unknown"));
    }

    @Test
    void testExtendTemporaryAssignment() {
        User user = userManager.findByUsername("john_doe").get();
        Role role = roleManager.findByName("Admin").get();
        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Test");

        TemporaryAssignment assignment = new TemporaryAssignment(user, role, metadata, "2026-06-30 23:59");
        assignmentManager.add(assignment);

        assignmentManager.extendTemporaryAssignment(assignment.assignmentId(), "2027-12-31 23:59");

        assertEquals("2027-12-31 23:59", assignment.getExpiresAt());
    }

    @Test
    void testExtendPermanentAssignment() {
        User user = userManager.findByUsername("john_doe").get();
        Role role = roleManager.findByName("Admin").get();
        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Test");

        PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
        assignmentManager.add(assignment);

        assertThrows(IllegalArgumentException.class,
                () -> assignmentManager.extendTemporaryAssignment(assignment.assignmentId(), "2027-12-31 23:59"));
    }

    @Test
    void testEqualsAndHashCode() {
        AssignmentManager manager1 = new AssignmentManager(userManager, roleManager);
        AssignmentManager manager2 = new AssignmentManager(userManager, roleManager);

        assertEquals(manager1, manager2);
        assertEquals(manager1.hashCode(), manager2.hashCode());
    }
}
