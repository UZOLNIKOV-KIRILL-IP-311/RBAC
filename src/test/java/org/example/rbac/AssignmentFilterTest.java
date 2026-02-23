package org.example.rbac;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentFilterTest {

    private final User user1 = User.create("john_doe", "John Doe", "john.doe@example.com");
    private final User user2 = User.create("jane_smith", "Jane Smith", "jane.smith@company.com");

    private final Role role1 = new Role("Admin", "Administrator role");
    private final Role role2 = new Role("Viewer", "Viewer role");

    private final AssignmentMetadata metadata1;
    private final AssignmentMetadata metadata2;

    public AssignmentFilterTest() {
        metadata1 = AssignmentMetadata.create("admin_user", "2026-01-15 10:00", "Initial assignment");
        metadata2 = AssignmentMetadata.create("system", "2026-02-20 14:30", "System assignment");
    }

    @Test
    void testByUser() {
        PermanentAssignment assignment1 = new PermanentAssignment(user1, role1, metadata1);
        PermanentAssignment assignment2 = new PermanentAssignment(user2, role1, metadata1);

        AssignmentFilter filter = AssignmentFilters.byUser(user1);
        assertTrue(filter.test(assignment1));
        assertFalse(filter.test(assignment2));
    }

    @Test
    void testByUsername() {
        PermanentAssignment assignment1 = new PermanentAssignment(user1, role1, metadata1);
        PermanentAssignment assignment2 = new PermanentAssignment(user2, role1, metadata1);

        AssignmentFilter filter = AssignmentFilters.byUsername("john_doe");
        assertTrue(filter.test(assignment1));
        assertFalse(filter.test(assignment2));
    }

    @Test
    void testByRole() {
        PermanentAssignment assignment1 = new PermanentAssignment(user1, role1, metadata1);
        PermanentAssignment assignment2 = new PermanentAssignment(user1, role2, metadata1);

        AssignmentFilter filter = AssignmentFilters.byRole(role1);
        assertTrue(filter.test(assignment1));
        assertFalse(filter.test(assignment2));
    }

    @Test
    void testByRoleName() {
        PermanentAssignment assignment1 = new PermanentAssignment(user1, role1, metadata1);
        PermanentAssignment assignment2 = new PermanentAssignment(user1, role2, metadata1);

        AssignmentFilter filter = AssignmentFilters.byRoleName("Admin");
        assertTrue(filter.test(assignment1));
        assertFalse(filter.test(assignment2));
    }

    @Test
    void testActiveOnly() {
        PermanentAssignment activeAssignment = new PermanentAssignment(user1, role1, metadata1);
        PermanentAssignment revokedAssignment = new PermanentAssignment(user1, role1, metadata1);
        revokedAssignment.revoke();

        AssignmentFilter filter = AssignmentFilters.activeOnly();
        assertTrue(filter.test(activeAssignment));
        assertFalse(filter.test(revokedAssignment));
    }

    @Test
    void testInactiveOnly() {
        PermanentAssignment activeAssignment = new PermanentAssignment(user1, role1, metadata1);
        PermanentAssignment revokedAssignment = new PermanentAssignment(user1, role1, metadata1);
        revokedAssignment.revoke();

        AssignmentFilter filter = AssignmentFilters.inactiveOnly();
        assertFalse(filter.test(activeAssignment));
        assertTrue(filter.test(revokedAssignment));
    }

    @Test
    void testByType() {
        PermanentAssignment permAssignment = new PermanentAssignment(user1, role1, metadata1);
        TemporaryAssignment tempAssignment = new TemporaryAssignment(user1, role1, metadata1, "2027-12-31 23:59");

        AssignmentFilter permFilter = AssignmentFilters.byType("PERMANENT");
        assertTrue(permFilter.test(permAssignment));
        assertFalse(permFilter.test(tempAssignment));

        AssignmentFilter tempFilter = AssignmentFilters.byType("TEMPORARY");
        assertFalse(tempFilter.test(permAssignment));
        assertTrue(tempFilter.test(tempAssignment));

        AssignmentFilter caseInsensitiveFilter = AssignmentFilters.byType("permanent");
        assertTrue(caseInsensitiveFilter.test(permAssignment));
    }

    @Test
    void testAssignedBy() {
        PermanentAssignment assignment1 = new PermanentAssignment(user1, role1, metadata1);
        PermanentAssignment assignment2 = new PermanentAssignment(user1, role1, metadata2);

        AssignmentFilter filter = AssignmentFilters.assignedBy("admin_user");
        assertTrue(filter.test(assignment1));
        assertFalse(filter.test(assignment2));
    }

    @Test
    void testAssignedAfter() {
        PermanentAssignment assignment1 = new PermanentAssignment(user1, role1, metadata1);
        PermanentAssignment assignment2 = new PermanentAssignment(user1, role1, metadata2);

        AssignmentFilter filter = AssignmentFilters.assignedAfter("2026-01-20 00:00");
        assertFalse(filter.test(assignment1));
        assertTrue(filter.test(assignment2));
    }

    @Test
    void testExpiringBefore() {
        TemporaryAssignment tempAssignment1 = new TemporaryAssignment(user1, role1, metadata1, "2026-06-30 23:59");
        TemporaryAssignment tempAssignment2 = new TemporaryAssignment(user1, role1, metadata1, "2027-12-31 23:59");
        PermanentAssignment permAssignment = new PermanentAssignment(user1, role1, metadata1);

        AssignmentFilter filter = AssignmentFilters.expiringBefore("2026-12-31 23:59");
        assertTrue(filter.test(tempAssignment1));
        assertFalse(filter.test(tempAssignment2));
        assertFalse(filter.test(permAssignment));
    }

    @Test
    void testAndCombination() {
        PermanentAssignment assignment1 = new PermanentAssignment(user1, role1, metadata1);
        PermanentAssignment assignment2 = new PermanentAssignment(user2, role1, metadata1);

        AssignmentFilter filter1 = AssignmentFilters.byRole(role1);
        AssignmentFilter filter2 = AssignmentFilters.byUsername("john_doe");
        AssignmentFilter combined = filter1.and(filter2);

        assertTrue(combined.test(assignment1));
        assertFalse(combined.test(assignment2));
    }

    @Test
    void testOrCombination() {
        PermanentAssignment assignment1 = new PermanentAssignment(user1, role1, metadata1);
        PermanentAssignment assignment2 = new PermanentAssignment(user2, role2, metadata1);

        AssignmentFilter filter1 = AssignmentFilters.byUsername("john_doe");
        AssignmentFilter filter2 = AssignmentFilters.byRoleName("Viewer");
        AssignmentFilter combined = filter1.or(filter2);

        assertTrue(combined.test(assignment1));
        assertTrue(combined.test(assignment2));
    }

    @Test
    void testByUserNull() {
        assertThrows(IllegalArgumentException.class, () -> AssignmentFilters.byUser(null));
    }

    @Test
    void testByUsernameNull() {
        assertThrows(IllegalArgumentException.class, () -> AssignmentFilters.byUsername(null));
    }

    @Test
    void testByRoleNull() {
        assertThrows(IllegalArgumentException.class, () -> AssignmentFilters.byRole(null));
    }

    @Test
    void testByRoleNameNull() {
        assertThrows(IllegalArgumentException.class, () -> AssignmentFilters.byRoleName(null));
    }

    @Test
    void testByTypeNull() {
        assertThrows(IllegalArgumentException.class, () -> AssignmentFilters.byType(null));
    }

    @Test
    void testAssignedByNull() {
        assertThrows(IllegalArgumentException.class, () -> AssignmentFilters.assignedBy(null));
    }

    @Test
    void testAssignedAfterNull() {
        assertThrows(IllegalArgumentException.class, () -> AssignmentFilters.assignedAfter(null));
    }

    @Test
    void testAssignedAfterInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> AssignmentFilters.assignedAfter("invalid-date"));
    }

    @Test
    void testExpiringBeforeNull() {
        assertThrows(IllegalArgumentException.class, () -> AssignmentFilters.expiringBefore(null));
    }

    @Test
    void testExpiringBeforeInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> AssignmentFilters.expiringBefore("invalid-date"));
    }
}
