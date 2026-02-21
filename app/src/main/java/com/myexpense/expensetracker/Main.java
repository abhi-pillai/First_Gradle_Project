package com.myexpense.expensetracker;

import com.myexpense.expensetracker.repository.*;
import com.myexpense.expensetracker.service.*;
import com.myexpense.expensetracker.ui.LoginFrame;

import javax.swing.*;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            
            // ── Repositories ─────────────────────────────
            UserRepository     userRepo     = new UserRepository();
            ExpenseRepository  expenseRepo  = new ExpenseRepository();
            IncomeRepository   incomeRepo   = new IncomeRepository();
            CategoryRepository categoryRepo = new CategoryRepository();

            // ── Services ─────────────────────────────────
            // CategoryService must be created before AuthService
            // so it can be injected for default-category seeding
            CategoryService categoryService = new CategoryService(categoryRepo);
            AuthService     authService     = new AuthService(userRepo, categoryService);
            ExpenseService  expenseService  = new ExpenseService(expenseRepo);
            IncomeService   incomeService   = new IncomeService(incomeRepo);
            ReportService   reportService   = new ReportService(expenseService, incomeService);
            BudgetService   budgetService   = new BudgetService(expenseService, categoryService);

            // ── Set look and feel ─────────────────────────
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            // ── Launch ────────────────────────────────────
            new LoginFrame(authService, expenseService, incomeService,
                    reportService, budgetService, categoryService).setVisible(true);
        });
    }
}