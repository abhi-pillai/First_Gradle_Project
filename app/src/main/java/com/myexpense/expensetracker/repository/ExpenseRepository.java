package com.myexpense.expensetracker.repository;

import com.myexpense.expensetracker.model.Expense;
import com.myexpense.expensetracker.model.PaymentMethod;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository {

    private static final String FILE_PATH = getDataPath("expenses.csv");

    private static String getDataPath(String filename) {
        String os   = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");
        String base;
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            base = (appData != null ? appData : home) + java.io.File.separator + "TrackIt" + java.io.File.separator + "data";
        } else if (os.contains("mac")) {
            base = home + "/Library/Application Support/TrackIt/data";
        } else {
            base = home + "/.local/share/TrackIt/data";
        }
        return base + java.io.File.separator + filename;
    }
    private static final String[] HEADER = {"id", "userId", "amount", "date", "category", "note", "paymentMethod"};

    public ExpenseRepository() {
        initFile();
    }

    private void initFile() {
        try {
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
                writeHeader(file);
                return;
            }
            if (!hasValidHeader(file)) {
                prependHeader(file);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error initialising expense file", e);
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
            return firstLine != null && firstLine.contains("userId") && firstLine.contains("amount");
        }
    }

    private void prependHeader(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("\"id\",\"userId\",\"amount\",\"date\",\"category\",\"note\",\"paymentMethod\"");
            for (String line : lines) pw.println(line);
        }
    }

    public void save(Expense expense) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
            writer.writeNext(toRow(expense));
        }
    }

    /**
     * Load expenses for a specific user only — fixes Bug 1.
     */
    public List<Expense> loadByUser(String userId) {
        List<Expense> expenses = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return expenses;

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean isFirstLine = true;
            while ((line = reader.readNext()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                if (line.length < 7) continue;

                // Only load records belonging to this user
                if (!line[1].equals(userId)) continue;

                expenses.add(new Expense(
                        line[0],
                        line[1],
                        Double.parseDouble(line[2]),
                        LocalDate.parse(line[3]),
                        line[4],
                        line[5],
                        PaymentMethod.valueOf(line[6])
                ));
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Error reading expense file", e);
        }
        return expenses;
    }

    /**
     * Overwrite only records belonging to userId; keep other users' data intact.
     */
    public void overwriteForUser(String userId, List<Expense> updatedExpenses) throws IOException {
        // Load ALL records (all users)
        List<Expense> allExpenses = loadAll();

        // Remove old records for this user, then add the updated ones
        List<Expense> others = new ArrayList<>();
        for (Expense e : allExpenses) {
            if (!e.getUserId().equals(userId)) others.add(e);
        }
        others.addAll(updatedExpenses);

        // Write everything back
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
            writer.writeNext(HEADER);
            for (Expense e : others) writer.writeNext(toRow(e));
        }
    }

    private List<Expense> loadAll() {
        List<Expense> expenses = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return expenses;
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean isFirstLine = true;
            while ((line = reader.readNext()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                if (line.length < 7) continue;
                expenses.add(new Expense(
                        line[0], line[1],
                        Double.parseDouble(line[2]),
                        LocalDate.parse(line[3]),
                        line[4], line[5],
                        PaymentMethod.valueOf(line[6])
                ));
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Error reading expense file", e);
        }
        return expenses;
    }

    private String[] toRow(Expense e) {
        return new String[]{
                e.getId(), e.getUserId(),
                String.valueOf(e.getAmount()),
                e.getDate().toString(),
                e.getCategory(), e.getNote(),
                e.getPaymentMethod().name()
        };
    }
}