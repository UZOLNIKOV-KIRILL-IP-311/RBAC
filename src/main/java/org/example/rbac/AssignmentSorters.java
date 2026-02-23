package org.example.rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

public class AssignmentSorters {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private AssignmentSorters() {
        // Utility class
    }

    public static Comparator<RoleAssignment> byUsername() {
        return Comparator.comparing(assignment -> assignment.user().username());
    }

    public static Comparator<RoleAssignment> byRoleName() {
        return Comparator.comparing(assignment -> assignment.role().getName());
    }

    public static Comparator<RoleAssignment> byAssignmentDate() {
        return (a1, a2) -> {
            try {
                LocalDateTime date1 = LocalDateTime.parse(a1.metadata().assignedAt(), DATE_FORMATTER);
                LocalDateTime date2 = LocalDateTime.parse(a2.metadata().assignedAt(), DATE_FORMATTER);
                return date1.compareTo(date2);
            } catch (DateTimeParseException e) {
                return a1.metadata().assignedAt().compareTo(a2.metadata().assignedAt());
            }
        };
    }
}
