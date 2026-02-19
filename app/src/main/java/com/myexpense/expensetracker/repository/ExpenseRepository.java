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

    private static final String FILE_PATH = "data/expenses.csv";

    public ExpenseRepository() {
        createFileIfNotExists();
    }

    private void createFileIfNotExists() {
        try {
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs();

            if (!file.exists()) {
                file.createNewFile();

                try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                    String[] header = {
                            "id", "amount", "date", "category", "note", "paymentMethod"
                    };
                    writer.writeNext(header);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating expense file", e);
        }
    }

    public void save(Expense expense) throws IOException {

        File file = new File(FILE_PATH);

        try (FileWriter writer = new FileWriter(file, true);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            String[] record = {
                    expense.getId(),
                    String.valueOf(expense.getAmount()),
                    expense.getDate().toString(),
                    expense.getCategory(),
                    expense.getNote(),
                    expense.getPaymentMethod().name()
            };

            csvWriter.writeNext(record);
        }
    }

    public List<Expense> loadAll() {

        List<Expense> expenses = new ArrayList<>();

        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return expenses;
        }

        try (CSVReader reader = new CSVReader(new FileReader(file))) {

            String[] line;
            boolean isFirstLine = true;

            while ((line = reader.readNext()) != null) {

                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // skip header
                }

                if (line.length < 6) continue;

                String id = line[0];
                double amount = Double.parseDouble(line[1]);
                LocalDate date = LocalDate.parse(line[2]);
                String category = line[3];
                String note = line[4];
                PaymentMethod paymentMethod = PaymentMethod.valueOf(line[5]);

                Expense expense = new Expense(
                        id,
                        amount,
                        date,
                        category,
                        note,
                        paymentMethod
                );

                expenses.add(expense);
            }

        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Error reading expense file", e);
        }

        return expenses;
    }
}
