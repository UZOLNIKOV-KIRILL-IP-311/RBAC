package org.example.rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AssignmentFilters {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private AssignmentFilters() {
        // Utility class
    }

    public static AssignmentFilter byUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return assignment -> user.equals(assignment.user());
    }

    public static AssignmentFilter byUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        return assignment -> username.equals(assignment.user().username());
    }

    public static AssignmentFilter byRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        return assignment -> role.equals(assignment.role());
    }

    public static AssignmentFilter byRoleName(String roleName) {
        if (roleName == null) {
            throw new IllegalArgumentException("Role name cannot be null");
        }
        return assignment -> roleName.equals(assignment.role().getName());
    }

    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly() {
        return assignment -> !assignment.isActive();
    }

    public static AssignmentFilter byType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        String normalizedType = type.toUpperCase();
        return assignment -> normalizedType.equals(assignment.assignmentType());
    }

    public static AssignmentFilter assignedBy(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        return assignment -> username.equals(assignment.metadata().assignedBy());
    }

    public static AssignmentFilter assignedAfter(String date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        LocalDateTime filterDate;
        try {
            filterDate = LocalDateTime.parse(date, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected: yyyy-MM-dd HH:mm", e);
        }

        return assignment -> {
            try {
                LocalDateTime assignedAt = LocalDateTime.parse(assignment.metadata().assignedAt(), DATE_FORMATTER);
                return assignedAt.isAfter(filterDate);
            } catch (DateTimeParseException e) {
                return false;
            }
        };
    }

    public static AssignmentFilter expiringBefore(String date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        LocalDateTime filterDate;
        try {
            filterDate = LocalDateTime.parse(date, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected: yyyy-MM-dd HH:mm", e);
        }

        return assignment -> {
            if (!(assignment instanceof TemporaryAssignment)) {
                return false;
            }
            TemporaryAssignment tempAssignment = (TemporaryAssignment) assignment;
            try {
                String expiresAt = tempAssignment.getExpiresAt();
                LocalDateTime expirationDate = LocalDateTime.parse(expiresAt, DATE_FORMATTER);
                return expirationDate.isBefore(filterDate);
            } catch (DateTimeParseException e) {
                return false;
            }
        };
    }
}
