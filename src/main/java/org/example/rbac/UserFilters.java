package org.example.rbac;

public class UserFilters {

    private UserFilters() {
        // Utility class
    }

    public static UserFilter byUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        return user -> username.equals(user.username());
    }

    public static UserFilter byUsernameContains(String substring) {
        if (substring == null) {
            throw new IllegalArgumentException("Substring cannot be null");
        }
        return user -> user.username().toLowerCase().contains(substring.toLowerCase());
    }

    public static UserFilter byEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        return user -> email.equals(user.email());
    }

    public static UserFilter byEmailDomain(String domain) {
        if (domain == null) {
            throw new IllegalArgumentException("Domain cannot be null");
        }
        String domainToCheck = domain.startsWith("@") ? domain : "@" + domain;
        return user -> user.email().toLowerCase().endsWith(domainToCheck.toLowerCase());
    }

    public static UserFilter byFullNameContains(String substring) {
        if (substring == null) {
            throw new IllegalArgumentException("Substring cannot be null");
        }
        return user -> user.fullName().toLowerCase().contains(substring.toLowerCase());
    }
}
