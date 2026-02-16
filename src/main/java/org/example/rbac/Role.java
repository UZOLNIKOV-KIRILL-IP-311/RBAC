package org.example.rbac;

import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.UUID;

public class Role {
    private final String id;
    private final String name;
    private final String description;
    private final Set<Permission> permissions;
 
    public Role(String name, String description) {
        if (name == null || description == null) {
            throw new IllegalArgumentException("Name and description cannot be null");
        }
        
        this.id = "role_" + UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>();
    }

    public void addPermission(Permission permission) {
        if (permission != null) {
            permissions.add(permission);
        }
    }

    public void removePermission(Permission permission) {
        if (permission != null) {
            permissions.remove(permission);
        }
    }

    public boolean hasPermission(Permission permission) {
        if (permission == null) {
            return false;
        }
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        if (permissionName == null || resource == null) {
            return false;
        }
        
        for (Permission perm : permissions) {
            if (perm.name().equalsIgnoreCase(permissionName) && 
                perm.resource().equalsIgnoreCase(resource)) {
                return true;
            }
        }
        return false;
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(new HashSet<>(permissions));
    }

    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Role role = (Role) obj;
        return id.equals(role.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return "Role{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", permissionsCount=" + permissions.size() +
                '}';
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: ").append(name).append(" [ID: ").append(id).append("]\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Permissions (").append(permissions.size()).append("):\n");
        
        for (Permission perm : permissions) {
            sb.append(" - ").append(perm.format()).append("\n");
        }

        if (permissions.size() > 0) {
            sb.setLength(sb.length() - 1);
        }
        
        return sb.toString();
    }
}