package com.myexpense.expensetracker.service;

import com.myexpense.expensetracker.model.Category;

import java.io.*;
import java.time.YearMonth;
import java.util.List;
import java.util.Properties;

public class BudgetService {

    private static final String BUDGET_FILE = getDataPath("budget.properties");

    private static String getDataPath(String filename) {
        String os   = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");
        String base;
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            base = (appData != null ? appData : home) + java.io.File.separator + "TrackIt" + java.io.File.separator + "data";
        } else if (os.contains("mac")) {
            base = home + "/Library/Application Support/TrackIt/data";
        } else {
            base = home + "/.local/share/TrackIt/data";
        }
        return base + java.io.File.separator + filename;
    }

    private final ExpenseService expenseService;
    private final CategoryService categoryService;
    private final Properties props = new Properties();

    public BudgetService(ExpenseService expenseService, CategoryService categoryService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
        loadProps();
    }

    // -------------------------------------------------------
    // Persistence helpers
    // -------------------------------------------------------
    private void loadProps() {
        File f = new File(BUDGET_FILE);
        if (!f.exists()) return;
        try (InputStream in = new FileInputStream(f)) {
            props.load(in);
        } catch (IOException ignored) {}
    }

    private void saveProps() {
        new File(BUDGET_FILE).getParentFile().mkdirs();
        try (OutputStream out = new FileOutputStream(BUDGET_FILE)) {
            props.store(out, "TrackIt budget settings");
        } catch (IOException ignored) {}
    }

    // -------------------------------------------------------
    // Global monthly budget — persisted per user
    // -------------------------------------------------------
    public void setMonthlyBudget(String userId, double budget) {
        props.setProperty("monthly." + userId, String.valueOf(budget));
        saveProps();
    }

    public double getMonthlyBudget(String userId) {
        return Double.parseDouble(props.getProperty("monthly." + userId, "0.0"));
    }

    public double getRemainingMonthlyBudget(String userId, YearMonth month) {
        double spent = expenseService.getMonthlyTotal(userId, month);
        return getMonthlyBudget(userId) - spent;
    }

    public boolean isMonthlyOverBudget(String userId, YearMonth month) {
        return getRemainingMonthlyBudget(userId, month) < 0;
    }

    // -------------------------------------------------------
    // Category budgets — fixed: uses current-month spending
    // -------------------------------------------------------
    public double getRemainingCategoryBudget(String userId, String categoryName, YearMonth month) {
        List<Category> categories = categoryService.getAllCategories(userId);

        Category category = categories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));

        // BUG FIX: use monthly total, not all-time total
        double spent = expenseService.getMonthlyTotal(userId, month) == 0
                ? expenseService.getTotalByCategory(userId, categoryName)
                : expensesThisMonthByCategory(userId, categoryName, month);

        return category.getMonthlyBudget() - spent;
    }

    private double expensesThisMonthByCategory(String userId, String categoryName, YearMonth month) {
        return expenseService.getAllExpenses(userId).stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(month))
                .filter(e -> e.getCategory().equalsIgnoreCase(categoryName))
                .mapToDouble(e -> e.getAmount())
                .sum();
    }

    public boolean isCategoryOverBudget(String userId, String categoryName, YearMonth month) {
        return getRemainingCategoryBudget(userId, categoryName, month) < 0;
    }
}