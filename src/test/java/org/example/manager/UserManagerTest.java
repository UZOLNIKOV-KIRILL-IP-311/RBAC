package org.example.manager;

import org.example.rbac.User;
import org.example.rbac.UserFilter;
import org.example.rbac.UserFilters;
import org.example.rbac.UserSorters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
    }

    @Test
    void testAddUser() {
        User user = User.create("john_doe", "John Doe", "john@example.com");
        userManager.add(user);

        assertEquals(1, userManager.count());
        assertTrue(userManager.exists("john_doe"));
    }

    @Test
    void testAddDuplicateUser() {
        User user1 = User.create("john_doe", "John Doe", "john@example.com");
        User user2 = User.create("john_doe", "John Doe Jr", "john2@example.com");

        userManager.add(user1);
        assertThrows(IllegalArgumentException.class, () -> userManager.add(user2));
    }

    @Test
    void testAddNullUser() {
        assertThrows(IllegalArgumentException.class, () -> userManager.add(null));
    }

    @Test
    void testRemoveUser() {
        User user = User.create("john_doe", "John Doe", "john@example.com");
        userManager.add(user);

        assertTrue(userManager.remove(user));
        assertEquals(0, userManager.count());
        assertFalse(userManager.exists("john_doe"));
    }

    @Test
    void testRemoveNonExistentUser() {
        User user = User.create("john_doe", "John Doe", "john@example.com");
        assertFalse(userManager.remove(user));
    }

    @Test
    void testFindById() {
        User user = User.create("john_doe", "John Doe", "john@example.com");
        userManager.add(user);

        Optional<User> found = userManager.findById("john_doe");
        assertTrue(found.isPresent());
        assertEquals("john_doe", found.get().username());

        Optional<User> notFound = userManager.findById("unknown");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindByIdNull() {
        Optional<User> found = userManager.findById(null);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        userManager.add(User.create("user1", "User One", "user1@example.com"));
        userManager.add(User.create("user2", "User Two", "user2@example.com"));

        List<User> users = userManager.findAll();
        assertEquals(2, users.size());
    }

    @Test
    void testCount() {
        assertEquals(0, userManager.count());
        userManager.add(User.create("user1", "User One", "user1@example.com"));
        assertEquals(1, userManager.count());
        userManager.add(User.create("user2", "User Two", "user2@example.com"));
        assertEquals(2, userManager.count());
    }

    @Test
    void testClear() {
        userManager.add(User.create("user1", "User One", "user1@example.com"));
        userManager.add(User.create("user2", "User Two", "user2@example.com"));
        userManager.clear();
        assertEquals(0, userManager.count());
    }

    @Test
    void testFindByUsername() {
        User user = User.create("john_doe", "John Doe", "john@example.com");
        userManager.add(user);

        Optional<User> found = userManager.findByUsername("john_doe");
        assertTrue(found.isPresent());
        assertEquals("john_doe", found.get().username());

        Optional<User> notFound = userManager.findByUsername("unknown");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindByEmail() {
        User user1 = User.create("john_doe", "John Doe", "john@example.com");
        User user2 = User.create("jane_smith", "Jane Smith", "jane@example.com");
        userManager.add(user1);
        userManager.add(user2);

        Optional<User> found = userManager.findByEmail("john@example.com");
        assertTrue(found.isPresent());
        assertEquals("john_doe", found.get().username());

        Optional<User> notFound = userManager.findByEmail("unknown@example.com");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindByFilter() {
        userManager.add(User.create("john_doe", "John Doe", "john@example.com"));
        userManager.add(User.create("jane_smith", "Jane Smith", "jane@company.com"));
        userManager.add(User.create("bob_wilson", "Bob Wilson", "bob@example.com"));

        UserFilter filter = UserFilters.byEmailDomain("example.com");
        List<User> filtered = userManager.findByFilter(filter);

        assertEquals(2, filtered.size());
    }

    @Test
    void testFindAllWithFilterAndSorter() {
        userManager.add(User.create("charlie", "Charlie Brown", "charlie@example.com"));
        userManager.add(User.create("alice", "Alice Smith", "alice@example.com"));
        userManager.add(User.create("bob", "Bob Johnson", "bob@company.com"));

        UserFilter filter = UserFilters.byEmailDomain("example.com");
        List<User> result = userManager.findAll(filter, UserSorters.byUsername());

        assertEquals(2, result.size());
        assertEquals("alice", result.get(0).username());
        assertEquals("charlie", result.get(1).username());
    }

    @Test
    void testExists() {
        assertFalse(userManager.exists("john_doe"));
        userManager.add(User.create("john_doe", "John Doe", "john@example.com"));
        assertTrue(userManager.exists("john_doe"));
    }

    @Test
    void testUpdate() {
        User user = User.create("john_doe", "John Doe", "john@example.com");
        userManager.add(user);

        userManager.update("john_doe", "John Updated", "john.updated@example.com");

        Optional<User> updated = userManager.findById("john_doe");
        assertTrue(updated.isPresent());
        assertEquals("John Updated", updated.get().fullName());
        assertEquals("john.updated@example.com", updated.get().email());
    }

    @Test
    void testUpdateNonExistentUser() {
        assertThrows(IllegalArgumentException.class,
                () -> userManager.update("unknown", "New Name", "new@example.com"));
    }

    @Test
    void testUpdateWithNullValues() {
        userManager.add(User.create("john_doe", "John Doe", "john@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> userManager.update("john_doe", null, "new@example.com"));
    }

    @Test
    void testEqualsAndHashCode() {
        UserManager manager1 = new UserManager();
        UserManager manager2 = new UserManager();

        assertEquals(manager1, manager2);
        assertEquals(manager1.hashCode(), manager2.hashCode());

        manager1.add(User.create("john_doe", "John Doe", "john@example.com"));
        assertNotEquals(manager1, manager2);
    }
}
