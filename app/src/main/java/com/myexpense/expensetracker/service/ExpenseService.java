package com.myexpense.expensetracker.service;

import com.myexpense.expensetracker.model.Expense;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ExpenseService {

    // Calculate total expenses
    public double calculateTotal(List<Expense> expenses) {
        return expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    // Filter by category
    public List<Expense> filterByCategory(List<Expense> expenses, String category) {
        return expenses.stream()
                .filter(e -> e.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    // Filter by date range
    public List<Expense> filterByDate(List<Expense> expenses, LocalDate start, LocalDate end) {
        return expenses.stream()
                .filter(e -> !e.getDate().isBefore(start) && !e.getDate().isAfter(end))
                .collect(Collectors.toList());
    }
}
