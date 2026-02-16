package org.example.rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private boolean autoRenew;

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt) {
        super(user, role, metadata);
        if (expiresAt == null) {
            throw new IllegalArgumentException("Expiration date cannot be null");
        }
        this.expiresAt = expiresAt;
        this.autoRenew = false;
    }
    
    @Override
    public boolean isActive() {
        if (isExpired()) {
            if (autoRenew) {
                return false;
            } else {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public void extend(String newExpirationDate) {
        if (newExpirationDate == null) {
            throw new IllegalArgumentException("New expiration date cannot be null");
        }
        this.expiresAt = newExpirationDate;
    }

    public boolean isExpired() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime expirationDateTime = LocalDateTime.parse(expiresAt, formatter);
            return LocalDateTime.now().isAfter(expirationDateTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd HH:mm", e);
        }
    }

    public String getTimeRemaining() {
        if (isExpired()) {
            return "EXPIRED";
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime expirationDateTime = LocalDateTime.parse(expiresAt, formatter);
            LocalDateTime now = LocalDateTime.now();
            
            long minutesDiff = java.time.Duration.between(now, expirationDateTime).toMinutes();
            
            if (minutesDiff < 60) {
                return minutesDiff + " minute(s) remaining";
            } else if (minutesDiff < 24 * 60) {
                long hours = minutesDiff / 60;
                return hours + " hour(s) and " + (minutesDiff % 60) + " minute(s) remaining";
            } else {
                long days = minutesDiff / (24 * 60);
                return days + " day(s), " + ((minutesDiff % (24 * 60)) / 60) + " hour(s) remaining";
            }
        } catch (DateTimeParseException e) {
            return "Invalid expiration date format";
        }
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }
    
    @Override
    public String summary() {
        String baseSummary = super.summary();
        return baseSummary + "\nExpiration: " + expiresAt + 
               "\nTime Remaining: " + getTimeRemaining() + 
               "\nAuto Renew: " + (autoRenew ? "YES" : "NO");
    }
} 