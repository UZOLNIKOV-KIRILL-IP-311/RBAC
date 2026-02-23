package org.example.rbac;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SortersTest {

    @Test
    void testUserSortersByUsername() {
        User user1 = User.create("charlie", "Charlie Brown", "charlie@example.com");
        User user2 = User.create("alice", "Alice Smith", "alice@example.com");
        User user3 = User.create("bob", "Bob Johnson", "bob@example.com");

        List<User> users = new ArrayList<>(List.of(user1, user2, user3));
        users.sort(UserSorters.byUsername());

        assertEquals("alice", users.get(0).username());
        assertEquals("bob", users.get(1).username());
        assertEquals("charlie", users.get(2).username());
    }

    @Test
    void testUserSortersByFullName() {
        User user1 = User.create("charlie", "Charlie Brown", "charlie@example.com");
        User user2 = User.create("alice", "Alice Smith", "alice@example.com");
        User user3 = User.create("bob", "Bob Johnson", "bob@example.com");

        List<User> users = new ArrayList<>(List.of(user1, user2, user3));
        users.sort(UserSorters.byFullName());

        assertEquals("Alice Smith", users.get(0).fullName());
        assertEquals("Bob Johnson", users.get(1).fullName());
        assertEquals("Charlie Brown", users.get(2).fullName());
    }

    @Test
    void testUserSortersByEmail() {
        User user1 = User.create("charlie", "Charlie Brown", "charlie@zebra.com");
        User user2 = User.create("alice", "Alice Smith", "alice@apple.com");
        User user3 = User.create("bob", "Bob Johnson", "bob@banana.com");

        List<User> users = new ArrayList<>(List.of(user1, user2, user3));
        users.sort(UserSorters.byEmail());

        assertEquals("alice@apple.com", users.get(0).email());
        assertEquals("bob@banana.com", users.get(1).email());
        assertEquals("charlie@zebra.com", users.get(2).email());
    }

    @Test
    void testRoleSortersByName() {
        Role role1 = new Role("Zebra", "Last role");
        Role role2 = new Role("Apple", "First role");
        Role role3 = new Role("Banana", "Middle role");

        List<Role> roles = new ArrayList<>(List.of(role1, role2, role3));
        roles.sort(RoleSorters.byName());

        assertEquals("Apple", roles.get(0).getName());
        assertEquals("Banana", roles.get(1).getName());
        assertEquals("Zebra", roles.get(2).getName());
    }

    @Test
    void testRoleSortersByPermissionCount() {
        Role role1 = new Role("Role1", "One permission");
        role1.addPermission(Permission.create("READ", "USERS", "Read users"));

        Role role2 = new Role("Role2", "Three permissions");
        role2.addPermission(Permission.create("READ", "USERS", "Read users"));
        role2.addPermission(Permission.create("WRITE", "USERS", "Write users"));
        role2.addPermission(Permission.create("DELETE", "USERS", "Delete users"));

        Role role3 = new Role("Role3", "Two permissions");
        role3.addPermission(Permission.create("READ", "USERS", "Read users"));
        role3.addPermission(Permission.create("WRITE", "USERS", "Write users"));

        List<Role> roles = new ArrayList<>(List.of(role1, role2, role3));
        roles.sort(RoleSorters.byPermissionCount());

        assertEquals(1, roles.get(0).getPermissions().size());
        assertEquals(2, roles.get(1).getPermissions().size());
        assertEquals(3, roles.get(2).getPermissions().size());
    }

    @Test
    void testAssignmentSortersByUsername() {
        User user1 = User.create("charlie", "Charlie Brown", "charlie@example.com");
        User user2 = User.create("alice", "Alice Smith", "alice@example.com");
        User user3 = User.create("bob", "Bob Johnson", "bob@example.com");

        Role role = new Role("Admin", "Admin role");
        AssignmentMetadata metadata = AssignmentMetadata.create("admin", "2026-01-15 10:00", "Test");

        List<RoleAssignment> assignments = new ArrayList<>();
        assignments.add(new PermanentAssignment(user1, role, metadata));
        assignments.add(new PermanentAssignment(user2, role, metadata));
        assignments.add(new PermanentAssignment(user3, role, metadata));

        assignments.sort(AssignmentSorters.byUsername());

        assertEquals("alice", assignments.get(0).user().username());
        assertEquals("bob", assignments.get(1).user().username());
        assertEquals("charlie", assignments.get(2).user().username());
    }

    @Test
    void testAssignmentSortersByRoleName() {
        User user = User.create("john_doe", "John Doe", "john@example.com");
        AssignmentMetadata metadata = AssignmentMetadata.create("admin", "2026-01-15 10:00", "Test");

        Role role1 = new Role("Zebra", "Last role");
        Role role2 = new Role("Apple", "First role");
        Role role3 = new Role("Banana", "Middle role");

        List<RoleAssignment> assignments = new ArrayList<>();
        assignments.add(new PermanentAssignment(user, role1, metadata));
        assignments.add(new PermanentAssignment(user, role2, metadata));
        assignments.add(new PermanentAssignment(user, role3, metadata));

        assignments.sort(AssignmentSorters.byRoleName());

        assertEquals("Apple", assignments.get(0).role().getName());
        assertEquals("Banana", assignments.get(1).role().getName());
        assertEquals("Zebra", assignments.get(2).role().getName());
    }

    @Test
    void testAssignmentSortersByAssignmentDate() {
        User user = User.create("john_doe", "John Doe", "john@example.com");
        Role role = new Role("Admin", "Admin role");

        AssignmentMetadata metadata1 = AssignmentMetadata.create("admin", "2026-03-15 10:00", "Test 1");
        AssignmentMetadata metadata2 = AssignmentMetadata.create("admin", "2026-01-15 10:00", "Test 2");
        AssignmentMetadata metadata3 = AssignmentMetadata.create("admin", "2026-02-15 10:00", "Test 3");

        List<RoleAssignment> assignments = new ArrayList<>();
        assignments.add(new PermanentAssignment(user, role, metadata1));
        assignments.add(new PermanentAssignment(user, role, metadata2));
        assignments.add(new PermanentAssignment(user, role, metadata3));

        assignments.sort(AssignmentSorters.byAssignmentDate());

        assertEquals("2026-01-15 10:00", assignments.get(0).metadata().assignedAt());
        assertEquals("2026-02-15 10:00", assignments.get(1).metadata().assignedAt());
        assertEquals("2026-03-15 10:00", assignments.get(2).metadata().assignedAt());
    }
}
