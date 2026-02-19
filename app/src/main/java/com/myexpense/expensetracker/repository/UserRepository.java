package com.myexpense.expensetracker.repository;

import com.myexpense.expensetracker.model.User;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    private static final String FILE_PATH = "data/users.csv";

    public UserRepository() {
        createFileIfNotExists();
    }

    private void createFileIfNotExists() {
        try {
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs();

            if (!file.exists()) {
                file.createNewFile();
                try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                    writer.writeNext(new String[]{"id", "username", "passwordHash"});
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating users file", e);
        }
    }

    // Save new user
    public void save(User user) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
            writer.writeNext(new String[]{
                    user.getId(),
                    user.getUsername(),
                    user.getPasswordHash()
            });
        }
    }

    // Load all users
    public List<User> loadAll() {
        List<User> users = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return users;

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean isFirstLine = true;
            while ((line = reader.readNext()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                if (line.length < 3) continue;

                User user = new User(line[0], line[1], line[2]);
                users.add(user);
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Error reading users file", e);
        }

        return users;
    }

    // Overwrite all users (for updates or deletion)
    public void overwriteAll(List<User> users) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
            writer.writeNext(new String[]{"id", "username", "passwordHash"});
            for (User u : users) {
                writer.writeNext(new String[]{
                        u.getId(),
                        u.getUsername(),
                        u.getPasswordHash()
                });
            }
        }
    }

    // Find user by username
    public Optional<User> findByUsername(String username) {
        return loadAll().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }
}
