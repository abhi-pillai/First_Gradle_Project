package com.myexpense.expensetracker.repository;

import com.myexpense.expensetracker.model.Expense;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
    
}
