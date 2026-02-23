package org.example.rbac;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleFilterTest {

    private final Permission readPermission = Permission.create("READ", "USERS", "Can read users");
    private final Permission writePermission = Permission.create("WRITE", "USERS", "Can write users");
    private final Permission deletePermission = Permission.create("DELETE", "REPORTS", "Can delete reports");

    private final Role role1;
    private final Role role2;
    private final Role role3;

    public RoleFilterTest() {
        role1 = new Role("Admin", "Administrator role");
        role1.addPermission(readPermission);
        role1.addPermission(writePermission);
        role1.addPermission(deletePermission);

        role2 = new Role("Viewer", "Viewer role");
        role2.addPermission(readPermission);

        role3 = new Role("Moderator", "Moderator role");
        role3.addPermission(readPermission);
        role3.addPermission(writePermission);
    }

    @Test
    void testByName() {
        RoleFilter filter = RoleFilters.byName("Admin");
        assertTrue(filter.test(role1));
        assertFalse(filter.test(role2));
        assertFalse(filter.test(role3));
    }

    @Test
    void testByNameContains() {
        RoleFilter filter = RoleFilters.byNameContains("er");
        assertFalse(filter.test(role1)); // Admin doesn't contain 'er'
        assertTrue(filter.test(role2)); // Viewer contains 'er'
        assertTrue(filter.test(role3)); // Moderator contains 'er'
    }

    @Test
    void testHasPermission() {
        RoleFilter filter = RoleFilters.hasPermission(readPermission);
        assertTrue(filter.test(role1));
        assertTrue(filter.test(role2));
        assertTrue(filter.test(role3));

        RoleFilter deleteFilter = RoleFilters.hasPermission(deletePermission);
        assertTrue(deleteFilter.test(role1));
        assertFalse(deleteFilter.test(role2));
        assertFalse(deleteFilter.test(role3));
    }

    @Test
    void testHasPermissionByNameAndResource() {
        RoleFilter filter = RoleFilters.hasPermission("READ", "USERS");
        assertTrue(filter.test(role1));
        assertTrue(filter.test(role2));
        assertTrue(filter.test(role3));

        RoleFilter deleteFilter = RoleFilters.hasPermission("DELETE", "REPORTS");
        assertTrue(deleteFilter.test(role1));
        assertFalse(deleteFilter.test(role2));
        assertFalse(deleteFilter.test(role3));
    }

    @Test
    void testHasAtLeastNPermissions() {
        RoleFilter filter1 = RoleFilters.hasAtLeastNPermissions(3);
        assertTrue(filter1.test(role1));
        assertFalse(filter1.test(role2));
        assertFalse(filter1.test(role3));

        RoleFilter filter2 = RoleFilters.hasAtLeastNPermissions(2);
        assertTrue(filter2.test(role1));
        assertFalse(filter2.test(role2));
        assertTrue(filter2.test(role3));

        RoleFilter filter3 = RoleFilters.hasAtLeastNPermissions(1);
        assertTrue(filter3.test(role1));
        assertTrue(filter3.test(role2));
        assertTrue(filter3.test(role3));
    }

    @Test
    void testAndCombination() {
        RoleFilter filter1 = RoleFilters.hasPermission(readPermission);
        RoleFilter filter2 = RoleFilters.hasPermission(writePermission);
        RoleFilter combined = filter1.and(filter2);

        assertTrue(combined.test(role1));
        assertFalse(combined.test(role2));
        assertTrue(combined.test(role3));
    }

    @Test
    void testOrCombination() {
        RoleFilter filter1 = RoleFilters.hasPermission(deletePermission);
        RoleFilter filter2 = RoleFilters.hasPermission(writePermission);
        RoleFilter combined = filter1.or(filter2);

        assertTrue(combined.test(role1));
        assertFalse(combined.test(role2));
        assertTrue(combined.test(role3));
    }

    @Test
    void testAndWithNull() {
        RoleFilter filter = RoleFilters.byName("Admin");
        RoleFilter combined = filter.and(null);
        assertTrue(combined.test(role1));
        assertFalse(combined.test(role2));
    }

    @Test
    void testOrWithNull() {
        RoleFilter filter = RoleFilters.byName("Admin");
        RoleFilter combined = filter.or(null);
        assertTrue(combined.test(role1));
        assertFalse(combined.test(role2));
    }

    @Test
    void testByNameNull() {
        assertThrows(IllegalArgumentException.class, () -> RoleFilters.byName(null));
    }

    @Test
    void testByNameContainsNull() {
        assertThrows(IllegalArgumentException.class, () -> RoleFilters.byNameContains(null));
    }

    @Test
    void testHasPermissionNull() {
        assertThrows(IllegalArgumentException.class, () -> RoleFilters.hasPermission((Permission) null));
    }

    @Test
    void testHasPermissionByNameAndResourceNull() {
        assertThrows(IllegalArgumentException.class, () -> RoleFilters.hasPermission(null, "USERS"));
        assertThrows(IllegalArgumentException.class, () -> RoleFilters.hasPermission("READ", null));
    }

    @Test
    void testHasAtLeastNPermissionsNegative() {
        assertThrows(IllegalArgumentException.class, () -> RoleFilters.hasAtLeastNPermissions(-1));
    }
}
