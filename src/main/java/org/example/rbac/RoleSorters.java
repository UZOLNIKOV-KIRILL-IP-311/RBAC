package org.example.rbac;

import java.util.Comparator;

public class RoleSorters {

    private RoleSorters() {
        // Utility class
    }

    public static Comparator<Role> byName() {
        return Comparator.comparing(Role::getName);
    }

    public static Comparator<Role> byPermissionCount() {
        return Comparator.comparingInt(role -> role.getPermissions().size());
    }
}
