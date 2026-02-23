package org.example.rbac;

@FunctionalInterface
public interface AssignmentFilter {
    boolean test(RoleAssignment assignment);

    default AssignmentFilter and(AssignmentFilter other) {
        if (other == null) {
            return this;
        }
        return assignment -> this.test(assignment) && other.test(assignment);
    }

    default AssignmentFilter or(AssignmentFilter other) {
        if (other == null) {
            return this;
        }
        return assignment -> this.test(assignment) || other.test(assignment);
    }
}
