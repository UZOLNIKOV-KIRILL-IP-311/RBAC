package org.example.manager;

import org.example.rbac.*;
import org.example.repository.Repository;

import java.util.*;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> assignmentsById;
    private final UserManager userManager;
    private final RoleManager roleManager;

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.assignmentsById = new HashMap<>();
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    @Override
    public void add(RoleAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment cannot be null");
        }

        // Проверяем существование пользователя и роли
        User user = assignment.user();
        Role role = assignment.role();

        if (!userManager.exists(user.username())) {
            throw new IllegalArgumentException("User '" + user.username() + "' not found");
        }

        if (!roleManager.exists(role.getName())) {
            throw new IllegalArgumentException("Role '" + role.getName() + "' not found");
        }

        // Проверяем на дублирование активной роли
        if (hasActiveAssignment(user, role)) {
            throw new IllegalArgumentException("User '" + user.username() + "' already has an active assignment for role '" + role.getName() + "'");
        }

        assignmentsById.put(assignment.assignmentId(), assignment);
    }

    private boolean hasActiveAssignment(User user, Role role) {
        return assignmentsById.values().stream()
                .filter(RoleAssignment::isActive)
                .anyMatch(a -> a.user().equals(user) && a.role().equals(role));
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) {
            return false;
        }
        return assignmentsById.remove(assignment.assignmentId()) != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(assignmentsById.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignmentsById.values());
    }

    @Override
    public int count() {
        return assignmentsById.size();
    }

    @Override
    public void clear() {
        assignmentsById.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        return assignmentsById.values().stream()
                .filter(a -> a.user().equals(user))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        if (role == null) {
            return Collections.emptyList();
        }
        return assignmentsById.values().stream()
                .filter(a -> a.role().equals(role))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return assignmentsById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        List<RoleAssignment> result = (filter != null) ? findByFilter(filter) : findAll();
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public List<RoleAssignment> getActiveAssignments() {
        return assignmentsById.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return assignmentsById.values().stream()
                .filter(assignment -> {
                    if (!(assignment instanceof TemporaryAssignment)) {
                        return false;
                    }
                    return ((TemporaryAssignment) assignment).isExpired();
                })
                .collect(Collectors.toList());
    }

    public boolean userHasRole(User user, Role role) {
        if (user == null || role == null) {
            return false;
        }
        return assignmentsById.values().stream()
                .filter(RoleAssignment::isActive)
                .anyMatch(a -> a.user().equals(user) && a.role().equals(role));
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        if (user == null || permissionName == null || resource == null) {
            return false;
        }

        Set<Permission> permissions = getUserPermissions(user);
        return permissions.stream()
                .anyMatch(p -> p.name().equalsIgnoreCase(permissionName) &&
                        p.resource().equalsIgnoreCase(resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        if (user == null) {
            return Collections.emptySet();
        }

        Set<Permission> permissions = new HashSet<>();
        assignmentsById.values().stream()
                .filter(RoleAssignment::isActive)
                .filter(a -> a.user().equals(user))
                .forEach(a -> permissions.addAll(a.role().getPermissions()));

        return permissions;
    }

    public void revokeAssignment(String assignmentId) {
        if (assignmentId == null) {
            throw new IllegalArgumentException("Assignment ID cannot be null");
        }

        RoleAssignment assignment = assignmentsById.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment with ID '" + assignmentId + "' not found");
        }

        if (assignment instanceof PermanentAssignment) {
            ((PermanentAssignment) assignment).revoke();
        } else if (assignment instanceof TemporaryAssignment) {
            // Для временных назначений просто помечаем как неактивное
            // В данной реализации TemporaryAssignment не имеет метода revoke
            // Поэтому мы можем только удалить назначение
            assignmentsById.remove(assignmentId);
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        if (assignmentId == null || newExpirationDate == null) {
            throw new IllegalArgumentException("Assignment ID and new expiration date cannot be null");
        }

        RoleAssignment assignment = assignmentsById.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment with ID '" + assignmentId + "' not found");
        }

        if (!(assignment instanceof TemporaryAssignment)) {
            throw new IllegalArgumentException("Assignment is not a temporary assignment");
        }

        ((TemporaryAssignment) assignment).extend(newExpirationDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AssignmentManager that = (AssignmentManager) obj;
        return assignmentsById.equals(that.assignmentsById);
    }

    @Override
    public int hashCode() {
        return assignmentsById.hashCode();
    }
}
