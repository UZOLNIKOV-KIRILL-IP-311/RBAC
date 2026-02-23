package org.example.rbac;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserFilterTest {

    private final User user1 = User.create("john_doe", "John Doe", "john.doe@example.com");
    private final User user2 = User.create("jane_smith", "Jane Smith", "jane.smith@company.com");
    private final User user3 = User.create("bob_wilson", "Bob Wilson", "bob.wilson@example.com");

    @Test
    void testByUsername() {
        UserFilter filter = UserFilters.byUsername("john_doe");
        assertTrue(filter.test(user1));
        assertFalse(filter.test(user2));
        assertFalse(filter.test(user3));
    }

    @Test
    void testByUsernameContains() {
        UserFilter filter = UserFilters.byUsernameContains("john");
        assertTrue(filter.test(user1));
        assertFalse(filter.test(user2));
        
        UserFilter caseInsensitiveFilter = UserFilters.byUsernameContains("JOHN");
        assertTrue(caseInsensitiveFilter.test(user1));
    }

    @Test
    void testByEmail() {
        UserFilter filter = UserFilters.byEmail("john.doe@example.com");
        assertTrue(filter.test(user1));
        assertFalse(filter.test(user2));
    }

    @Test
    void testByEmailDomain() {
        UserFilter exampleFilter = UserFilters.byEmailDomain("example.com");
        assertTrue(exampleFilter.test(user1));
        assertFalse(exampleFilter.test(user2));
        assertTrue(exampleFilter.test(user3));

        UserFilter companyFilter = UserFilters.byEmailDomain("@company.com");
        assertFalse(companyFilter.test(user1));
        assertTrue(companyFilter.test(user2));
    }

    @Test
    void testByFullNameContains() {
        UserFilter filter = UserFilters.byFullNameContains("john");
        assertTrue(filter.test(user1));
        assertFalse(filter.test(user2));
        
        UserFilter caseInsensitiveFilter = UserFilters.byFullNameContains("DOE");
        assertTrue(caseInsensitiveFilter.test(user1));
    }

    @Test
    void testAndCombination() {
        UserFilter filter1 = UserFilters.byUsernameContains("john");
        UserFilter filter2 = UserFilters.byEmailDomain("example.com");
        UserFilter combined = filter1.and(filter2);

        assertTrue(combined.test(user1));
        assertFalse(combined.test(user2));
        assertFalse(combined.test(user3));
    }

    @Test
    void testOrCombination() {
        UserFilter filter1 = UserFilters.byUsername("john_doe");
        UserFilter filter2 = UserFilters.byUsername("jane_smith");
        UserFilter combined = filter1.or(filter2);

        assertTrue(combined.test(user1));
        assertTrue(combined.test(user2));
        assertFalse(combined.test(user3));
    }

    @Test
    void testAndWithNull() {
        UserFilter filter = UserFilters.byUsername("john_doe");
        UserFilter combined = filter.and(null);
        assertTrue(combined.test(user1));
        assertFalse(combined.test(user2));
    }

    @Test
    void testOrWithNull() {
        UserFilter filter = UserFilters.byUsername("john_doe");
        UserFilter combined = filter.or(null);
        assertTrue(combined.test(user1));
        assertFalse(combined.test(user2));
    }

    @Test
    void testByUsernameNull() {
        assertThrows(IllegalArgumentException.class, () -> UserFilters.byUsername(null));
    }

    @Test
    void testByUsernameContainsNull() {
        assertThrows(IllegalArgumentException.class, () -> UserFilters.byUsernameContains(null));
    }

    @Test
    void testByEmailNull() {
        assertThrows(IllegalArgumentException.class, () -> UserFilters.byEmail(null));
    }

    @Test
    void testByEmailDomainNull() {
        assertThrows(IllegalArgumentException.class, () -> UserFilters.byEmailDomain(null));
    }

    @Test
    void testByFullNameContainsNull() {
        assertThrows(IllegalArgumentException.class, () -> UserFilters.byFullNameContains(null));
    }
}
