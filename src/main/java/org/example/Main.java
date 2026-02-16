package org.example;

import org.example.rbac.User;
import org.example.rbac.Permission;
import org.example.rbac.Role;
import org.example.rbac.AssignmentMetadata;
import org.example.rbac.PermanentAssignment;
import org.example.rbac.TemporaryAssignment;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.out.println("Testing User validation...");

        try {
            User validUser = User.create("john_doe", "John Doe", "john.doe@example.com");
            System.out.println("Valid user created: " + validUser.format());
        } catch (Exception e) {
            System.out.println("Unexpected error for valid user: " + e.getMessage());
        }

        String[] invalidUsernames = {
            null,           // null
            "",             // empty
            "ab",           // too short
            "a".repeat(21), // too long
            "user name",    // contains space
            "user@name",    // contains @
            "user-name"     // contains -
        };
        
        for (String username : invalidUsernames) {
            try {
                User.create(username, "Test User", "test@example.com");
                System.out.println("ERROR: Username '" + username + "' should have been invalid but was accepted");
            } catch (IllegalArgumentException e) {
                System.out.println("Correctly rejected invalid username '" + username + "': " + e.getMessage());
            }
        }

        String[] validUsernames = {"abc", "user_123", "test_user_name", "validuser"};
        for (String username : validUsernames) {
            try {
                User user = User.create(username, "Test User", "test@example.com");
                System.out.println("Valid username '" + username + "' accepted: " + user.format());
            } catch (Exception e) {
                System.out.println("ERROR: Valid username '" + username + "' was rejected: " + e.getMessage());
            }
        }

        String[] invalidEmails = {
            null,               // null
            "",                 // empty
            "invalid-email",    // no @
            "@example.com",     // no local part
            "user@",            // no domain
            "user@example",     // no TLD
            "user..name@example.com" // double dots
        };
        
        for (String email : invalidEmails) {
            try {
                User.create("testuser", "Test User", email);
                System.out.println("ERROR: Email '" + email + "' should have been invalid but was accepted");
            } catch (IllegalArgumentException e) {
                System.out.println("Correctly rejected invalid email '" + email + "': " + e.getMessage());
            }
        }
        
        System.out.println("\nTesting Permission validation...");

        try {
            Permission validPerm = Permission.create("READ", "USERS", "Can read user data");
            System.out.println("Valid permission created: " + validPerm.format());
        } catch (Exception e) {
            System.out.println("Unexpected error for valid permission: " + e.getMessage());
        }

        Permission perm = Permission.create("read", "Users", "Can read user data");
        System.out.println("Normalized permission: " + perm.format());
        System.out.println("Name: " + perm.name() + ", Resource: " + perm.resource());

        try {
            Permission.create("READ WRITE", "USERS", "Can read and write user data");
            System.out.println("ERROR: Permission with space in name should have been rejected");
        } catch (IllegalArgumentException e) {
            System.out.println("Correctly rejected permission with space in name: " + e.getMessage());
        }
        
        System.out.println("\nTesting Role functionality...");

        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(Permission.create("READ", "USERS", "Can view user list"));
        adminRole.addPermission(Permission.create("WRITE", "USERS", "Can create and edit users"));
        adminRole.addPermission(Permission.create("DELETE", "USERS", "Can delete users"));
        
        System.out.println(adminRole.format());

        System.out.println("Has READ permission on USERS: " + 
                          adminRole.hasPermission(Permission.create("READ", "USERS", "Can view user list")));
        System.out.println("Has READ permission (by name/resource): " + 
                          adminRole.hasPermission("READ", "USERS"));
        
        System.out.println("\nTesting Assignment functionality...");

        User testUser = User.create("testuser", "Test User", "test@example.com");
        Role viewerRole = new Role("Viewer", "Limited access role");
        viewerRole.addPermission(Permission.create("READ", "REPORTS", "Can view reports"));

        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Initial setup");

        PermanentAssignment permAssign = new PermanentAssignment(testUser, viewerRole, metadata);
        System.out.println("Permanent assignment summary:");
        System.out.println(permAssign.summary());

        TemporaryAssignment tempAssign = new TemporaryAssignment(testUser, adminRole, metadata, "2026-12-31 23:59");
        System.out.println("\nTemporary assignment summary:");
        System.out.println(tempAssign.summary());
        
        System.out.println("\nAll tests completed!");
    }
}
