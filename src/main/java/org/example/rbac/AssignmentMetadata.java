package org.example.rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    public AssignmentMetadata {
        if (assignedBy == null) {
            throw new IllegalArgumentException("Assigned by cannot be null");
        }
        if (assignedAt == null) {
            throw new IllegalArgumentException("Assigned at cannot be null");
        }
    }

    public static AssignmentMetadata create(String assignedBy, String assignedAt, String reason) {
        String normalizedReason = (reason != null) ? reason : "";
        return new AssignmentMetadata(assignedBy, assignedAt, normalizedReason);
    }

    public static AssignmentMetadata now(String assignedBy, String reason) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return create(assignedBy, currentTime, reason);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Assigned by: ").append(assignedBy).append("\n");
        sb.append("Assigned at: ").append(assignedAt).append("\n");
        if (reason != null && !reason.isEmpty()) {
            sb.append("Reason: ").append(reason).append("\n");
        }

        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}