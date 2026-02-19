package com.myexpense.expensetracker.repository;

import com.myexpense.expensetracker.model.Category;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    private static final String FILE_PATH = "data/categories.csv";

    public CategoryRepository() {
        createFileIfNotExists();
    }

    private void createFileIfNotExists() {
        try {
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs();

            if (!file.exists()) {
                file.createNewFile();
                try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                    writer.writeNext(new String[]{"id", "name", "monthlyBudget"});
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating category file", e);
        }
    }

    // Save new category
    public void save(Category category) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
            writer.writeNext(new String[]{
                    category.getId(),
                    category.getName(),
                    String.valueOf(category.getMonthlyBudget())
            });
        }
    }

    // Load all categories
    public List<Category> loadAll() {
        List<Category> categories = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return categories;

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean isFirstLine = true;
            while ((line = reader.readNext()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                if (line.length < 3) continue;

                Category category = new Category(
                        line[0],
                        line[1],
                        Double.parseDouble(line[2])
                );
                categories.add(category);
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Error reading category file", e);
        }
        return categories;
    }

    // Overwrite all categories (for update/delete)
    public void overwriteAll(List<Category> categories) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
            writer.writeNext(new String[]{"id", "name", "monthlyBudget"});
            for (Category c : categories) {
                writer.writeNext(new String[]{
                        c.getId(),
                        c.getName(),
                        String.valueOf(c.getMonthlyBudget())
                });
            }
        }
    }
}
