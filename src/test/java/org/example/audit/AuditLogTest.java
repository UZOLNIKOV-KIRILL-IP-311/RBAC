package org.example.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @Test
    void testLog() {
        auditLog.log("CREATE_USER", "admin", "john_doe", "New user created");

        List<AuditLog.AuditEntry> entries = auditLog.getAll();
        assertEquals(1, entries.size());

        AuditLog.AuditEntry entry = entries.get(0);
        assertEquals("CREATE_USER", entry.action());
        assertEquals("admin", entry.performer());
        assertEquals("john_doe", entry.target());
        assertEquals("New user created", entry.details());
        assertNotNull(entry.timestamp());
    }

    @Test
    void testLogWithNullDetails() {
        auditLog.log("DELETE_USER", "admin", "john_doe", null);

        List<AuditLog.AuditEntry> entries = auditLog.getAll();
        assertEquals(1, entries.size());
        assertEquals("", entries.get(0).details());
    }

    @Test
    void testGetAll() {
        auditLog.log("CREATE_USER", "admin", "user1", "");
        auditLog.log("CREATE_ROLE", "admin", "role1", "");
        auditLog.log("ASSIGN_ROLE", "admin", "user1", "");

        List<AuditLog.AuditEntry> entries = auditLog.getAll();
        assertEquals(3, entries.size());
    }

    @Test
    void testGetByPerformer() {
        auditLog.log("CREATE_USER", "admin", "user1", "");
        auditLog.log("CREATE_USER", "manager", "user2", "");
        auditLog.log("DELETE_USER", "admin", "user3", "");

        List<AuditLog.AuditEntry> adminEntries = auditLog.getByPerformer("admin");
        assertEquals(2, adminEntries.size());

        List<AuditLog.AuditEntry> managerEntries = auditLog.getByPerformer("manager");
        assertEquals(1, managerEntries.size());

        List<AuditLog.AuditEntry> unknownEntries = auditLog.getByPerformer("unknown");
        assertEquals(0, unknownEntries.size());
    }

    @Test
    void testGetByPerformerNull() {
        auditLog.log("CREATE_USER", "admin", "user1", "");

        List<AuditLog.AuditEntry> allEntries = auditLog.getByPerformer(null);
        assertEquals(1, allEntries.size());
    }

    @Test
    void testGetByAction() {
        auditLog.log("CREATE_USER", "admin", "user1", "");
        auditLog.log("CREATE_ROLE", "admin", "role1", "");
        auditLog.log("DELETE_USER", "admin", "user2", "");

        List<AuditLog.AuditEntry> createEntries = auditLog.getByAction("CREATE_USER");
        assertEquals(1, createEntries.size());

        List<AuditLog.AuditEntry> deleteEntries = auditLog.getByAction("DELETE_USER");
        assertEquals(1, deleteEntries.size());

        List<AuditLog.AuditEntry> unknownEntries = auditLog.getByAction("UNKNOWN");
        assertEquals(0, unknownEntries.size());
    }

    @Test
    void testGetByActionNull() {
        auditLog.log("CREATE_USER", "admin", "user1", "");

        List<AuditLog.AuditEntry> allEntries = auditLog.getByAction(null);
        assertEquals(1, allEntries.size());
    }

    @Test
    void testGetByTarget() {
        auditLog.log("CREATE_USER", "admin", "user1", "");
        auditLog.log("CREATE_ROLE", "admin", "role1", "");
        auditLog.log("UPDATE_USER", "admin", "user1", "");

        List<AuditLog.AuditEntry> user1Entries = auditLog.getByTarget("user1");
        assertEquals(2, user1Entries.size());

        List<AuditLog.AuditEntry> role1Entries = auditLog.getByTarget("role1");
        assertEquals(1, role1Entries.size());
    }

    @Test
    void testSize() {
        assertEquals(0, auditLog.size());

        auditLog.log("CREATE_USER", "admin", "user1", "");
        assertEquals(1, auditLog.size());

        auditLog.log("CREATE_ROLE", "admin", "role1", "");
        assertEquals(2, auditLog.size());
    }

    @Test
    void testClear() {
        auditLog.log("CREATE_USER", "admin", "user1", "");
        auditLog.log("CREATE_ROLE", "admin", "role1", "");

        auditLog.clear();

        assertEquals(0, auditLog.size());
        assertTrue(auditLog.getAll().isEmpty());
    }

    @Test
    void testSaveToFile() throws IOException {
        String testFile = "test-audit-log.txt";

        auditLog.log("CREATE_USER", "admin", "user1", "Test user");
        auditLog.log("CREATE_ROLE", "admin", "role1", "Test role");

        auditLog.saveToFile(testFile);

        File file = new File(testFile);
        assertTrue(file.exists());

        String content = new String(Files.readAllBytes(Paths.get(testFile)));
        assertTrue(content.contains("AUDIT LOG"));
        assertTrue(content.contains("CREATE_USER"));
        assertTrue(content.contains("CREATE_ROLE"));
        assertTrue(content.contains("admin"));

        // Cleanup
        file.delete();
    }

    @Test
    void testSaveToFileWithEmptyFilename() {
        assertThrows(IllegalArgumentException.class, () -> auditLog.saveToFile(""));
        assertThrows(IllegalArgumentException.class, () -> auditLog.saveToFile(null));
    }

    @Test
    void testAuditEntryToString() {
        AuditLog.AuditEntry entry = new AuditLog.AuditEntry(
                "2024-01-15 10:30:00",
                "CREATE_USER",
                "admin",
                "john_doe",
                "Test details"
        );

        String expected = "[2024-01-15 10:30:00] CREATE_USER by admin on john_doe";
        assertEquals(expected, entry.toString());
    }
}
