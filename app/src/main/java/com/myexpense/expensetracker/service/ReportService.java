package com.myexpense.expensetracker.service;

import com.myexpense.expensetracker.model.Expense;

import java.time.YearMonth;
import java.util.*;

public class ReportService {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    public ReportService(ExpenseService expenseService, IncomeService incomeService) {
        this.expenseService = expenseService;
        this.incomeService = incomeService;
    }

    public double getTotalIncome(String userId) {
        return incomeService.getTotalIncome(userId);
    }

    public double getTotalExpenses(String userId) {
        return expenseService.getTotalExpenses(userId);
    }

    public double getNetSavings(String userId) {
        return getTotalIncome(userId) - getTotalExpenses(userId);
    }

    public Map<String, Double> getMonthlySummary(String userId, YearMonth month) {
        double income  = incomeService.getMonthlyTotal(userId, month);
        double expense = expenseService.getMonthlyTotal(userId, month);
        Map<String, Double> summary = new LinkedHashMap<>();
        summary.put("Income",  income);
        summary.put("Expense", expense);
        summary.put("Savings", income - expense);
        return summary;
    }

    /**
     * Category breakdown — all-time for the user.
     */
    public Map<String, Double> getCategoryBreakdown(String userId) {
        List<Expense> expenses = expenseService.getAllExpenses(userId);
        Map<String, Double> breakdown = new LinkedHashMap<>();
        for (Expense e : expenses) {
            breakdown.merge(e.getCategory(), e.getAmount(), Double::sum);
        }
        // Sort descending by value
        breakdown.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(en -> breakdown.put(en.getKey(), en.getValue()));
        return breakdown;
    }

    /**
     * Category breakdown scoped to a specific month.
     */
    public Map<String, Double> getCategoryBreakdownForMonth(String userId, YearMonth month) {
        List<Expense> expenses = expenseService.getAllExpenses(userId);
        Map<String, Double> breakdown = new LinkedHashMap<>();
        for (Expense e : expenses) {
            if (YearMonth.from(e.getDate()).equals(month)) {
                breakdown.merge(e.getCategory(), e.getAmount(), Double::sum);
            }
        }
        return breakdown;
    }
}