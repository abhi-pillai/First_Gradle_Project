package com.myexpense.expensetracker.repository;

import com.myexpense.expensetracker.model.Income;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IncomeRepository {

    private static final String FILE_PATH = "data/income.csv";

    public IncomeRepository() {
        createFileIfNotExists();
    }

    private void createFileIfNotExists() {
        try {
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
                try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                    writer.writeNext(new String[]{"id", "amount", "date", "source", "note"});
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating income file", e);
        }
    }

    // Save a new income
    public void save(Income income) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
            writer.writeNext(new String[]{
                    income.getId(),
                    String.valueOf(income.getAmount()),
                    income.getDate().toString(),
                    income.getSource(),
                    income.getNote()
            });
        }
    }

    // Load all income
    public List<Income> loadAll() {
        List<Income> incomes = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return incomes;

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean isFirstLine = true;
            while ((line = reader.readNext()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (line.length < 5) continue;
                Income income = new Income(
                        line[0],
                        Double.parseDouble(line[1]),
                        LocalDate.parse(line[2]),
                        line[3],
                        line[4]
                );
                incomes.add(income);
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Error reading income file", e);
        }

        return incomes;
    }

    // Overwrite all incomes (for update/delete)
    public void overwriteAll(List<Income> incomes) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
            writer.writeNext(new String[]{"id", "amount", "date", "source", "note"});
            for (Income i : incomes) {
                writer.writeNext(new String[]{
                        i.getId(),
                        String.valueOf(i.getAmount()),
                        i.getDate().toString(),
                        i.getSource(),
                        i.getNote()
                });
            }
        }
    }
}
