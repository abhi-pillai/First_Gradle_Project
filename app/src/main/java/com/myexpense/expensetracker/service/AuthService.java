package com.myexpense.expensetracker.service;

import com.myexpense.expensetracker.model.User;
import com.myexpense.expensetracker.repository.UserRepository;
import com.myexpense.expensetracker.util.PasswordUtil;

import java.io.IOException;
import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private User loggedInUser;

    public AuthService(UserRepository userRepository, CategoryService categoryService) {
        this.userRepository = userRepository;
        this.categoryService = categoryService;
    }

    // =========================
    // REGISTER
    // =========================
    public void register(String username, String password) throws IOException {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            throw new RuntimeException("Username already exists!");
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);
        User user = new User(username, hash, salt);
        userRepository.save(user);

        // Seed default categories for the new user
        categoryService.seedDefaultCategories(user.getId());
    }

    // =========================
    // LOGIN
    // =========================
    public boolean login(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) return false;

        User user = userOptional.get();

        // Support legacy accounts with no salt
        boolean valid;
        if (user.getSalt() == null || user.getSalt().isEmpty()) {
            // Legacy: plain SHA-256 with no salt — accept but migrate
            valid = legacyCheck(password, user.getPasswordHash());
            if (valid) migratePassword(user, password);
        } else {
            String hash = PasswordUtil.hashPassword(password, user.getSalt());
            valid = user.getPasswordHash().equals(hash);
        }

        if (valid) loggedInUser = userRepository.findByUsername(username).get();
        return valid;
    }

    private boolean legacyCheck(String password, String storedHash) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString().equals(storedHash);
        } catch (Exception e) {
            return false;
        }
    }

    private void migratePassword(User user, String plainPassword) {
        try {
            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hashPassword(plainPassword, salt);
            user.setSalt(salt);
            user.setPasswordHash(hash);
            java.util.List<User> all = userRepository.loadAll();
            all.replaceAll(u -> u.getId().equals(user.getId()) ? user : u);
            userRepository.overwriteAll(all);
        } catch (IOException ignored) {}
    }

    public void logout() {
        loggedInUser = null;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}