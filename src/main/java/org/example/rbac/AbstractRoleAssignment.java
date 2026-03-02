package org.example.rbac;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {
    protected final String assignmentId;
    protected final User user;
    protected final Role role;
    protected final AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        if (user == null || role == null || metadata == null) {
            throw new IllegalArgumentException("User, role, and metadata cannot be null");
        }
        
        this.assignmentId = "assign_" + UUID.randomUUID().toString();
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }
    
    @Override
    public String assignmentId() {
        return assignmentId;
    }
    
    @Override
    public User user() {
        return user;
    }
    
    @Override
    public Role role() {
        return role;
    }
    
    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }
    
    @Override
    public abstract boolean isActive();
    
    @Override
    public abstract String assignmentType();
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AbstractRoleAssignment that = (AbstractRoleAssignment) obj;
        return assignmentId.equals(that.assignmentId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(assignmentType()).append("] ")
          .append(role.getName()).append(" assigned to ")
          .append(user.username()).append(" by ")
          .append(metadata.assignedBy()).append(" at ")
          .append(metadata.assignedAt()).append("\n");

        if (metadata.reason() != null && !metadata.reason().isEmpty()) {
            sb.append("Reason: ").append(metadata.reason()).append("\n");
        }

        sb.append("Status: ").append(isActive() ? "ACTIVE" : "INACTIVE");

        return sb.toString();
    }
}