package com.myexpense.expensetracker.repository;

import com.myexpense.expensetracker.model.Category;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    private static final String FILE_PATH = getDataPath("categories.csv");

    private static String getDataPath(String filename) {
        String appData = System.getenv("APPDATA");
        String base = (appData != null)
                ? appData + java.io.File.separator + "TrackIt" + java.io.File.separator + "data"
                : "data";
        return base + java.io.File.separator + filename;
    }
    private static final String[] HEADER = {"id", "userId", "name", "monthlyBudget"};

    public CategoryRepository() {
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
            throw new RuntimeException("Error initialising category file", e);
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
            return firstLine != null && firstLine.contains("userId") && firstLine.contains("monthlyBudget");
        }
    }

    private void prependHeader(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("\"id\",\"userId\",\"name\",\"monthlyBudget\"");
            for (String line : lines) pw.println(line);
        }
    }

    public void save(Category category) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
            writer.writeNext(toRow(category));
        }
    }

    public List<Category> loadByUser(String userId) {
        List<Category> categories = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return categories;

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean isFirstLine = true;
            while ((line = reader.readNext()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                if (line.length < 4) continue;
                if (!line[1].equals(userId)) continue;

                categories.add(new Category(
                        line[0], line[1], line[2],
                        Double.parseDouble(line[3])
                ));
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Error reading category file", e);
        }
        return categories;
    }

    public void overwriteForUser(String userId, List<Category> updatedCategories) throws IOException {
        List<Category> all = loadAll();
        List<Category> others = new ArrayList<>();
        for (Category c : all) {
            if (!c.getUserId().equals(userId)) others.add(c);
        }
        others.addAll(updatedCategories);

        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
            writer.writeNext(HEADER);
            for (Category c : others) writer.writeNext(toRow(c));
        }
    }

    private List<Category> loadAll() {
        List<Category> categories = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return categories;
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean isFirstLine = true;
            while ((line = reader.readNext()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                if (line.length < 4) continue;
                categories.add(new Category(
                        line[0], line[1], line[2], Double.parseDouble(line[3])
                ));
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Error reading category file", e);
        }
        return categories;
    }

    private String[] toRow(Category c) {
        return new String[]{
                c.getId(), c.getUserId(), c.getName(),
                String.valueOf(c.getMonthlyBudget())
        };
    }
}