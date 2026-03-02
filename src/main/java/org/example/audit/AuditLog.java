package org.example.audit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Система логирования событий (аудит).
 */
public class AuditLog {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<AuditEntry> entries;

    public AuditLog() {
        this.entries = new ArrayList<>();
    }

    /**
     * Запись события в лог.
     *
     * @param action действие (CREATE, DELETE, UPDATE, ASSIGN, REVOKE и т.д.)
     * @param performer кто выполнил действие
     * @param target целевой объект (username, roleName и т.д.)
     * @param details дополнительные детали
     */
    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details != null ? details : "");
        entries.add(entry);
    }

    /**
     * Получить все записи лога.
     *
     * @return список всех записей
     */
    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    /**
     * Получить записи по исполнителю.
     *
     * @param performer исполнитель
     * @return отфильтрованный список записей
     */
    public List<AuditEntry> getByPerformer(String performer) {
        if (performer == null) {
            return getAll();
        }
        return entries.stream()
                .filter(entry -> performer.equals(entry.performer()))
                .collect(Collectors.toList());
    }

    /**
     * Получить записи по типу действия.
     *
     * @param action тип действия
     * @return отфильтрованный список записей
     */
    public List<AuditEntry> getByAction(String action) {
        if (action == null) {
            return getAll();
        }
        return entries.stream()
                .filter(entry -> action.equalsIgnoreCase(entry.action()))
                .collect(Collectors.toList());
    }

    /**
     * Получить записи по целевому объекту.
     *
     * @param target целевой объект
     * @return отфильтрованный список записей
     */
    public List<AuditEntry> getByTarget(String target) {
        if (target == null) {
            return getAll();
        }
        return entries.stream()
                .filter(entry -> target.equals(entry.target()))
                .collect(Collectors.toList());
    }

    /**
     * Форматированный вывод всех записей лога.
     */
    public void printLog() {
        if (entries.isEmpty()) {
            System.out.println("Audit log is empty.");
            return;
        }

        System.out.println("====================================== AUDIT LOG ======================================");
        System.out.printf("%-20s | %-12s | %-15s | %-20s | %s%n",
                "Timestamp", "Action", "Performer", "Target", "Details");
        System.out.println("--------------------------------------------------------------------------------------");

        for (AuditEntry entry : entries) {
            String details = entry.details();
            if (details.length() > 40) {
                details = details.substring(0, 37) + "...";
            }
            System.out.printf("%-20s | %-12s | %-15s | %-20s | %s%n",
                    entry.timestamp(),
                    entry.action(),
                    entry.performer(),
                    entry.target(),
                    details);
        }

        System.out.println("======================================================================================");
        System.out.println("Total entries: " + entries.size());
    }

    /**
     * Сохранение лога в файл.
     *
     * @param filename имя файла для сохранения
     */
    public void saveToFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("AUDIT LOG\n");
            writer.write("Generated: " + LocalDateTime.now().format(DATE_TIME_FORMATTER) + "\n");
            writer.write("================================================================================\n\n");

            for (AuditEntry entry : entries) {
                writer.write("[" + entry.timestamp() + "] ");
                writer.write("ACTION: " + entry.action() + " | ");
                writer.write("PERFORMER: " + entry.performer() + " | ");
                writer.write("TARGET: " + entry.target() + " | ");
                writer.write("DETAILS: " + entry.details() + "\n");
            }

            writer.write("\n================================================================================\n");
            writer.write("Total entries: " + entries.size() + "\n");

            System.out.println("Audit log saved to: " + filename);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save audit log to file: " + filename, e);
        }
    }

    /**
     * Очистить лог.
     */
    public void clear() {
        entries.clear();
    }

    /**
     * Количество записей в логе.
     *
     * @return количество записей
     */
    public int size() {
        return entries.size();
    }

    /**
     * Запись аудита.
     *
     * @param timestamp время события
     * @param action действие
     * @param performer исполнитель
     * @param target целевой объект
     * @param details детали
     */
    public record AuditEntry(
            String timestamp,
            String action,
            String performer,
            String target,
            String details
    ) {
        @Override
        public String toString() {
            return "[" + timestamp + "] " + action + " by " + performer + " on " + target;
        }
    }
}
