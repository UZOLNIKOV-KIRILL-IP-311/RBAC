package org.example.rbac;

import java.util.regex.Pattern;

public record User(String username, String fullName, String email) {
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern EMAIL_CONTIGUOUS_DOTS_PATTERN = Pattern.compile(".*\\.\\..*");

    public User {
        if (username == null || fullName == null || email == null) {
            throw new IllegalArgumentException("All fields must be non-null");
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                "Username must contain only Latin letters, digits, and underscores, and be 3-20 characters long"
            );
        }

        if (fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        
        if (EMAIL_CONTIGUOUS_DOTS_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email format is invalid: contains contiguous dots");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email format is invalid");
        }
    }
    
    public static User create(String username, String fullName, String email) {
        return new User(username, fullName, email);
    }
    
    public String format() {
        return username + " (" + fullName + ") <" + email + ">";
    }
}