package com.myexpense.expensetracker;

import com.myexpense.expensetracker.model.Expense;
import com.myexpense.expensetracker.repository.ExpenseRepository;
import com.myexpense.expensetracker.service.ExpenseService;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        

        ExpenseRepository repository = new ExpenseRepository();
        ExpenseService service = new ExpenseService();

        // try {
        //     repository.save(expense);
        //     System.out.println("Expense saved successfully!");
        // } catch (IOException e) {
        //     System.out.println("Error saving expense: " + e.getMessage());
        // }

        try{
            List<Expense> expenses = repository.loadAll();
            System.out.println("All Expenses:");
            for (Expense e : expenses) {
                System.out.println(
                        e.getAmount() + " | " +
                        e.getCategory() + " | " +
                        e.getDate()
                );
            }
        
       
        double total = service.calculateTotal(expenses);
        System.out.println("Total Expenses: " + total);

        // Filter by category example
        List<Expense> foodExpenses = service.filterByCategory(expenses, "Food");
        System.out.println("Food Expenses:");
        foodExpenses.forEach(e -> System.out.println(e.getAmount() + " | " + e.getDate()));

        // Filter by date range example
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();
        List<Expense> lastWeek = service.filterByDate(expenses, start, end);
        System.out.println("Last 7 Days Expenses:");
        lastWeek.forEach(e -> System.out.println(e.getAmount() + " | " + e.getCategory() + " | " + e.getDate()));
        } catch (IOException | CsvValidationException e) {
            System.out.println("Error loading expenses: " + e.getMessage());
        }
    }
}
