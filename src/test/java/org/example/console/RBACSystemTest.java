package org.example.console;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RBACSystemTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
    }

    @Test
    void testConstructor() {
        assertNotNull(system.getUserManager());
        assertNotNull(system.getRoleManager());
        assertNotNull(system.getAssignmentManager());
        assertEquals("system", system.getCurrentUser());
    }

    @Test
    void testSetCurrentUser() {
        system.setCurrentUser("admin");
        assertEquals("admin", system.getCurrentUser());
    }

    @Test
    void testSetCurrentUserNull() {
        assertThrows(IllegalArgumentException.class, () -> system.setCurrentUser(null));
    }

    @Test
    void testSetCurrentUserEmpty() {
        assertThrows(IllegalArgumentException.class, () -> system.setCurrentUser(""));
    }

    @Test
    void testInitialize() {
        system.initialize();
        assertTrue(system.getUserManager().exists("admin"));
        assertTrue(system.getUserManager().exists("manager"));
        assertTrue(system.getUserManager().exists("viewer"));
        assertEquals(3, system.getUserManager().count());
        assertEquals(3, system.getRoleManager().count());
        assertEquals(3, system.getAssignmentManager().count());
    }

    @Test
    void testGenerateStatistics() {
        system.initialize();
        String stats = system.generateStatistics();
        assertNotNull(stats);
        assertTrue(stats.contains("RBAC SYSTEM STATISTICS"));
        assertTrue(stats.contains("Users: 3"));
        assertTrue(stats.contains("Roles: 3"));
        assertTrue(stats.contains("Current user: system"));
    }
}
