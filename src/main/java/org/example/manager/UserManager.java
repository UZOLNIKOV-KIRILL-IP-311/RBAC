package org.example.manager;

import org.example.rbac.User;
import org.example.rbac.UserFilter;
import org.example.repository.Repository;

import java.util.*;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {
    private final Map<String, User> usersByUsername;

    public UserManager() {
        this.usersByUsername = new HashMap<>();
    }

    @Override
    public void add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (usersByUsername.containsKey(user.username())) {
            throw new IllegalArgumentException("User with username '" + user.username() + "' already exists");
        }
        usersByUsername.put(user.username(), user);
    }

    @Override
    public boolean remove(User user) {
        if (user == null) {
            return false;
        }
        return usersByUsername.remove(user.username()) != null;
    }

    @Override
    public Optional<User> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByUsername.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(usersByUsername.values());
    }

    @Override
    public int count() {
        return usersByUsername.size();
    }

    @Override
    public void clear() {
        usersByUsername.clear();
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByUsername.get(username));
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return usersByUsername.values().stream()
                .filter(user -> email.equals(user.email()))
                .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return usersByUsername.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        List<User> result = (filter != null) ? findByFilter(filter) : findAll();
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public boolean exists(String username) {
        if (username == null) {
            return false;
        }
        return usersByUsername.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail) {
        if (username == null || newFullName == null || newEmail == null) {
            throw new IllegalArgumentException("Username, fullName, and email cannot be null");
        }

        User existingUser = usersByUsername.get(username);
        if (existingUser == null) {
            throw new IllegalArgumentException("User with username '" + username + "' not found");
        }

        // Валидация новых данных через создание нового User
        User updatedUser = User.create(username, newFullName, newEmail);
        usersByUsername.put(username, updatedUser);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserManager that = (UserManager) obj;
        return usersByUsername.equals(that.usersByUsername);
    }

    @Override
    public int hashCode() {
        return usersByUsername.hashCode();
    }
}
