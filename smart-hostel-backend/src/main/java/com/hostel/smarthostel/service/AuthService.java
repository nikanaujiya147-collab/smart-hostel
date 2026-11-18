package com.hostel.smarthostel.service;

import com.hostel.smarthostel.exception.ForbiddenException;
import com.hostel.smarthostel.exception.ResourceNotFoundException;
import com.hostel.smarthostel.model.User;
import com.hostel.smarthostel.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Deliberately simple authentication (login is not the focus of this project).
 * Every API call sends the caller's id in the X-User-Id header; require() checks the caller's type.
 */
@Service
public class AuthService {

    private final UserRepository users;

    public AuthService(UserRepository users) { this.users = users; }

    public User login(String username, String password) {
        User user = users.findByUsername(username == null ? "" : username)
                .orElseThrow(() -> new ForbiddenException("Invalid username or password"));
        if (!user.checkPassword(password)) throw new ForbiddenException("Invalid username or password");
        return user;
    }

    /** Returns the caller if they are an instance of the required class (Student, Warden, ...). */
    public <T extends User> T require(Long userId, Class<T> type) {
        User user = requireAny(userId);
        if (!type.isInstance(user)) {
            throw new ForbiddenException("Role " + user.getRole() + " is not allowed to do this");
        }
        return type.cast(user);
    }

    public User requireAny(Long userId) {
        if (userId == null) throw new ForbiddenException("X-User-Id header is required");
        return users.findById(userId)
                .orElseThrow(() -> new ForbiddenException("Unknown user id " + userId));
    }

    public <T extends User> T findById(Long id, Class<T> type, String label) {
        User user = users.findById(id).orElseThrow(() -> new ResourceNotFoundException(label + " " + id + " not found"));
        if (!type.isInstance(user)) throw new ResourceNotFoundException(label + " " + id + " not found");
        return type.cast(user);
    }
}
