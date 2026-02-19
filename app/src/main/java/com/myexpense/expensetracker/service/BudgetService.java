package com.myexpense.expensetracker.service;

import com.myexpense.expensetracker.model.Category;

import java.time.YearMonth;
import java.util.List;

public class BudgetService {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;

    private double monthlyBudget; // global monthly budget

    public BudgetService(ExpenseService expenseService,
                         CategoryService categoryService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
    }

    // =========================
    // GLOBAL MONTHLY BUDGET
    // =========================

    public void setMonthlyBudget(double budget) {
        this.monthlyBudget = budget;
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    public double getRemainingMonthlyBudget(YearMonth month) {
        double spent = expenseService.getMonthlyTotal(month);
        return monthlyBudget - spent;
    }

    // =========================
    // CATEGORY BUDGET
    // =========================

    public double getRemainingCategoryBudget(String categoryName) {

        List<Category> categories = categoryService.getAllCategories();

        Category category = categories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found"));

        double spent = expenseService.getTotalByCategory(categoryName);

        return category.getMonthlyBudget() - spent;
    }

    public boolean isCategoryOverBudget(String categoryName) {
        return getRemainingCategoryBudget(categoryName) < 0;
    }

    public boolean isMonthlyOverBudget(YearMonth month) {
        return getRemainingMonthlyBudget(month) < 0;
    }
}
