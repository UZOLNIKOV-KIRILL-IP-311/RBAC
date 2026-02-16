package org.example.rbac;

public record Permission(String name, String resource, String description) {

    public Permission {

        if (name == null || resource == null || description == null) {
            throw new IllegalArgumentException("All fields must be non-null");
        }

        String normalizedName = name.toUpperCase().trim();
        if (normalizedName.contains(" ")) {
            throw new IllegalArgumentException("Permission name cannot contain spaces");
        }

        String normalizedResource = resource.toLowerCase().trim();

        if (description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }

    }

    public static Permission create(String name, String resource, String description) {

        String normalizedName = name.toUpperCase().trim();
        String normalizedResource = resource.toLowerCase().trim();
        String normalizedDescription = description.trim();

        if (normalizedName.contains(" ")) {
            throw new IllegalArgumentException("Permission name cannot contain spaces");
        }
        if (normalizedDescription.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        
        return new Permission(normalizedName, normalizedResource, normalizedDescription);
    }

    public String format() {
        return name + " on " + resource + ": " + description;
    }

    public boolean matches(String namePattern, String resourcePattern) {
        if (namePattern != null && !name().toUpperCase().contains(namePattern.toUpperCase())) {
            return false;
        }
        if (resourcePattern != null && !resource().toLowerCase().contains(resourcePattern.toLowerCase())) {
            return false;
        }
        return true;
    }
}