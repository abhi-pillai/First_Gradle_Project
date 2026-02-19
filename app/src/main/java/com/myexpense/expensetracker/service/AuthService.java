package com.myexpense.expensetracker.service;

import com.myexpense.expensetracker.model.User;
import com.myexpense.expensetracker.repository.UserRepository;
import com.myexpense.expensetracker.util.PasswordUtil;

import java.io.IOException;
import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;
    private User loggedInUser;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =========================
    // REGISTER
    // =========================
    public void register(String username, String password) throws IOException {

        Optional<User> existingUser = userRepository.findByUsername(username);

        if (existingUser.isPresent()) {
            throw new RuntimeException("Username already exists!");
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        User user = new User(username, hashedPassword);

        userRepository.save(user);
    }

    // =========================
    // LOGIN
    // =========================
    public boolean login(String username, String password) {

        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        String hashedInput = PasswordUtil.hashPassword(password);

        if (user.getPasswordHash().equals(hashedInput)) {
            loggedInUser = user;
            return true;
        }

        return false;
    }

    public void logout() {
        loggedInUser = null;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}
