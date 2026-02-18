package com.myexpense.expensetracker;

import com.myexpense.expensetracker.model.Expense;
import com.myexpense.expensetracker.repository.ExpenseRepository;

import java.io.IOException;
import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {

        Expense expense = new Expense(
                250.0,
                LocalDate.now(),
                "Food",
                "Lunch with friends",
                "Card"
        );

        ExpenseRepository repository = new ExpenseRepository();

        try {
            repository.save(expense);
            System.out.println("Expense saved successfully!");
        } catch (IOException e) {
            System.out.println("Error saving expense: " + e.getMessage());
        }
    }
}
