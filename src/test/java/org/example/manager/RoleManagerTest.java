package org.example.manager;

import org.example.rbac.Permission;
import org.example.rbac.Role;
import org.example.rbac.RoleFilter;
import org.example.rbac.RoleFilters;
import org.example.rbac.RoleSorters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoleManagerTest {

    private RoleManager roleManager;

    @BeforeEach
    void setUp() {
        roleManager = new RoleManager();
    }

    @Test
    void testAddRole() {
        Role role = new Role("Admin", "Administrator role");
        roleManager.add(role);

        assertEquals(1, roleManager.count());
        assertTrue(roleManager.exists("Admin"));
    }

    @Test
    void testAddDuplicateRole() {
        Role role1 = new Role("Admin", "Administrator role");
        Role role2 = new Role("Admin", "Admin role duplicate");

        roleManager.add(role1);
        assertThrows(IllegalArgumentException.class, () -> roleManager.add(role2));
    }

    @Test
    void testAddNullRole() {
        assertThrows(IllegalArgumentException.class, () -> roleManager.add(null));
    }

    @Test
    void testRemoveRole() {
        Role role = new Role("Admin", "Administrator role");
        roleManager.add(role);

        assertTrue(roleManager.remove(role));
        assertEquals(0, roleManager.count());
        assertFalse(roleManager.exists("Admin"));
    }

    @Test
    void testRemoveNonExistentRole() {
        Role role = new Role("Admin", "Administrator role");
        assertFalse(roleManager.remove(role));
    }

    @Test
    void testFindById() {
        Role role = new Role("Admin", "Administrator role");
        roleManager.add(role);

        Optional<Role> found = roleManager.findById(role.getId());
        assertTrue(found.isPresent());
        assertEquals("Admin", found.get().getName());

        Optional<Role> notFound = roleManager.findById("unknown");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindAll() {
        roleManager.add(new Role("Admin", "Administrator role"));
        roleManager.add(new Role("Viewer", "Viewer role"));

        List<Role> roles = roleManager.findAll();
        assertEquals(2, roles.size());
    }

    @Test
    void testCount() {
        assertEquals(0, roleManager.count());
        roleManager.add(new Role("Admin", "Administrator role"));
        assertEquals(1, roleManager.count());
    }

    @Test
    void testClear() {
        roleManager.add(new Role("Admin", "Administrator role"));
        roleManager.add(new Role("Viewer", "Viewer role"));
        roleManager.clear();
        assertEquals(0, roleManager.count());
    }

    @Test
    void testFindByName() {
        Role role = new Role("Admin", "Administrator role");
        roleManager.add(role);

        Optional<Role> found = roleManager.findByName("Admin");
        assertTrue(found.isPresent());
        assertEquals("Admin", found.get().getName());

        Optional<Role> notFound = roleManager.findByName("Unknown");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindByFilter() {
        Role role1 = new Role("Admin", "Administrator role");
        role1.addPermission(Permission.create("READ", "USERS", "Read users"));
        role1.addPermission(Permission.create("WRITE", "USERS", "Write users"));

        Role role2 = new Role("Viewer", "Viewer role");
        role2.addPermission(Permission.create("READ", "USERS", "Read users"));

        Role role3 = new Role("Guest", "Guest role");

        roleManager.add(role1);
        roleManager.add(role2);
        roleManager.add(role3);

        RoleFilter filter = RoleFilters.hasAtLeastNPermissions(2);
        List<Role> filtered = roleManager.findByFilter(filter);

        assertEquals(1, filtered.size());
        assertEquals("Admin", filtered.get(0).getName());
    }

    @Test
    void testFindAllWithFilterAndSorter() {
        Role role1 = new Role("Zebra", "Last role");
        role1.addPermission(Permission.create("READ", "USERS", "Read users"));

        Role role2 = new Role("Apple", "First role");
        role2.addPermission(Permission.create("READ", "USERS", "Read users"));
        role2.addPermission(Permission.create("WRITE", "USERS", "Write users"));

        Role role3 = new Role("Banana", "Middle role");
        role3.addPermission(Permission.create("READ", "USERS", "Read users"));

        roleManager.add(role1);
        roleManager.add(role2);
        roleManager.add(role3);

        RoleFilter filter = RoleFilters.hasAtLeastNPermissions(1);
        List<Role> result = roleManager.findAll(filter, RoleSorters.byName());

        assertEquals(3, result.size());
        assertEquals("Apple", result.get(0).getName());
        assertEquals("Banana", result.get(1).getName());
        assertEquals("Zebra", result.get(2).getName());
    }

    @Test
    void testExists() {
        assertFalse(roleManager.exists("Admin"));
        roleManager.add(new Role("Admin", "Administrator role"));
        assertTrue(roleManager.exists("Admin"));
    }

    @Test
    void testAddPermissionToRole() {
        Role role = new Role("Admin", "Administrator role");
        roleManager.add(role);

        Permission permission = Permission.create("READ", "USERS", "Read users");
        roleManager.addPermissionToRole("Admin", permission);

        Optional<Role> updated = roleManager.findByName("Admin");
        assertTrue(updated.isPresent());
        assertTrue(updated.get().hasPermission(permission));
    }

    @Test
    void testAddPermissionToNonExistentRole() {
        assertThrows(IllegalArgumentException.class,
                () -> roleManager.addPermissionToRole("Unknown", Permission.create("READ", "USERS", "Read")));
    }

    @Test
    void testRemovePermissionFromRole() {
        Role role = new Role("Admin", "Administrator role");
        Permission permission = Permission.create("READ", "USERS", "Read users");
        role.addPermission(permission);
        roleManager.add(role);

        roleManager.removePermissionFromRole("Admin", permission);

        Optional<Role> updated = roleManager.findByName("Admin");
        assertTrue(updated.isPresent());
        assertFalse(updated.get().hasPermission(permission));
    }

    @Test
    void testFindRolesWithPermission() {
        Role role1 = new Role("Admin", "Administrator role");
        role1.addPermission(Permission.create("READ", "USERS", "Read users"));

        Role role2 = new Role("Viewer", "Viewer role");
        role2.addPermission(Permission.create("READ", "USERS", "Read users"));

        Role role3 = new Role("Editor", "Editor role");
        role3.addPermission(Permission.create("WRITE", "USERS", "Write users"));

        roleManager.add(role1);
        roleManager.add(role2);
        roleManager.add(role3);

        List<Role> rolesWithReadPermission = roleManager.findRolesWithPermission("READ", "USERS");
        assertEquals(2, rolesWithReadPermission.size());
    }

    @Test
    void testEqualsAndHashCode() {
        RoleManager manager1 = new RoleManager();
        RoleManager manager2 = new RoleManager();

        assertEquals(manager1, manager2);
        assertEquals(manager1.hashCode(), manager2.hashCode());

        manager1.add(new Role("Admin", "Administrator role"));
        assertNotEquals(manager1, manager2);
    }
}
