package com.myexpense.expensetracker.repository;

import com.myexpense.expensetracker.model.Expense;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository {

    private static final String FILE_PATH = "data/expenses.csv";

    public void save(Expense expense) throws IOException {

         File file = new File(FILE_PATH);
         file.getParentFile().mkdirs(); // creates folder if missing

    try (FileWriter writer = new FileWriter(file, true);
         CSVWriter csvWriter = new CSVWriter(writer)) {

        String[] record = {
                String.valueOf(expense.getAmount()),
                expense.getDate().toString(),
                expense.getCategory(),
                expense.getNote(),
                expense.getPaymentMethod()
        };

        csvWriter.writeNext(record);
        }
    }

    public List<Expense> loadAll() throws IOException, CsvValidationException {

    List<Expense> expenses = new ArrayList<>();

    File file = new File(FILE_PATH);
    if (!file.exists()) {
        return expenses; // return empty list if file not found
    }

   try (CSVReader reader = new CSVReader(new FileReader(file))) {
        String[] line;
        while (true) {
            try {
                line = reader.readNext();
                if (line == null) break;
            } catch (CsvValidationException e) {
                System.out.println("CSV validation error: " + e.getMessage());
                continue;
            }
            if (line.length == 0 || line[0].isBlank()) continue;
            double amount = Double.parseDouble(line[0]);
            LocalDate date = LocalDate.parse(line[1]);
            String category = line[2];
            String note = line[3];
            String paymentMethod = line[4];

            Expense expense = new Expense(amount, date, category, note, paymentMethod);
            expenses.add(expense);
        }
    } 

    

    return expenses;
}

    
}
