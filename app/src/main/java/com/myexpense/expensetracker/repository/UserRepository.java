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

    private static final String FILE_PATH = getDataPath("users.csv");

    private static String getDataPath(String filename) {
        String appData = System.getenv("APPDATA");
        String base = (appData != null)
                ? appData + java.io.File.separator + "TrackIt" + java.io.File.separator + "data"
                : "data";
        return base + java.io.File.separator + filename;
    }
    private static final String[] HEADER = {"id", "username", "passwordHash", "salt"};

    public UserRepository() {
        initFile();
    }

    // -------------------------------------------------------
    // Bug 3 fix: ensure file exists AND has a valid header
    // -------------------------------------------------------
    private void initFile() {
        try {
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs();

            if (!file.exists()) {
                file.createNewFile();
                writeHeader(file);
                return;
            }

            // File exists — check if first line is the correct header
            if (!hasValidHeader(file)) {
                prependHeader(file);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error initialising users file", e);
        }
    }

    private void writeHeader(File file) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            writer.writeNext(HEADER);
        }
    }

    private boolean hasValidHeader(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String firstLine = br.readLine();
            return firstLine != null && firstLine.contains("id") && firstLine.contains("username");
        }
    }

    private void prependHeader(File file) throws IOException {
        List<String> lines = new java.util.ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // Write header manually (CSV-style)
            pw.println("\"id\",\"username\",\"passwordHash\",\"salt\"");
            for (String line : lines) pw.println(line);
        }
    }

    public void save(User user) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
            writer.writeNext(new String[]{
                    user.getId(),
                    user.getUsername(),
                    user.getPasswordHash(),
                    user.getSalt()
            });
        }
    }

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

                // Support legacy rows that have no salt column
                String salt = line.length >= 4 ? line[3] : "";
                users.add(new User(line[0], line[1], line[2], salt));
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Error reading users file", e);
        }
        return users;
    }

    public void overwriteAll(List<User> users) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
            writer.writeNext(HEADER);
            for (User u : users) {
                writer.writeNext(new String[]{
                        u.getId(), u.getUsername(), u.getPasswordHash(), u.getSalt()
                });
            }
        }
    }

    public Optional<User> findByUsername(String username) {
        return loadAll().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }
}