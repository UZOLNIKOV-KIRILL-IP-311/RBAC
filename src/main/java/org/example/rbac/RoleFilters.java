package org.example.rbac;

public class RoleFilters {

    private RoleFilters() {
        // Utility class
    }

    public static RoleFilter byName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        return role -> name.equals(role.getName());
    }

    public static RoleFilter byNameContains(String substring) {
        if (substring == null) {
            throw new IllegalArgumentException("Substring cannot be null");
        }
        return role -> role.getName().toLowerCase().contains(substring.toLowerCase());
    }

    public static RoleFilter hasPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }
        return role -> role.hasPermission(permission);
    }

    public static RoleFilter hasPermission(String permissionName, String resource) {
        if (permissionName == null || resource == null) {
            throw new IllegalArgumentException("Permission name and resource cannot be null");
        }
        return role -> role.hasPermission(permissionName, resource);
    }

    public static RoleFilter hasAtLeastNPermissions(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Number of permissions cannot be negative");
        }
        return role -> role.getPermissions().size() >= n;
    }
}
