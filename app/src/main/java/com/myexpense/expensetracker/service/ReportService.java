package com.myexpense.expensetracker.service;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.myexpense.expensetracker.model.Expense;

public class ReportService {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    public ReportService(ExpenseService expenseService,
                         IncomeService incomeService) {
        this.expenseService = expenseService;
        this.incomeService = incomeService;
    }

    // =========================
    // INCOME VS EXPENSE SUMMARY
    // =========================

    public double getTotalIncome() {
        return incomeService.getTotalIncome();
    }

    public double getTotalExpenses() {
        return expenseService.getTotalExpenses();
    }

    public double getNetSavings() {
        return getTotalIncome() - getTotalExpenses();
    }

    // =========================
    // MONTHLY SUMMARY
    // =========================

    public Map<String, Double> getMonthlySummary(YearMonth month) {

        double income = incomeService.getMonthlyTotal(month);
        double expense = expenseService.getMonthlyTotal(month);
        double savings = income - expense;

        Map<String, Double> summary = new HashMap<>();
        summary.put("Income", income);
        summary.put("Expense", expense);
        summary.put("Savings", savings);

        return summary;
    }

    // =========================
    // CATEGORY BREAKDOWN
    // =========================

    public Map<String, Double> getCategoryBreakdown() {

        List<Expense> expenses = expenseService.getAllExpenses();
        Map<String, Double> breakdown = new HashMap<>();

        for (Expense e : expenses) {
            breakdown.put(
                    e.getCategory(),
                    breakdown.getOrDefault(e.getCategory(), 0.0)
                            + e.getAmount()
            );
        }

        return breakdown;
    }
}
