package com.myexpense.expensetracker;

import com.myexpense.expensetracker.repository.*;
import com.myexpense.expensetracker.service.*;
import com.myexpense.expensetracker.ui.LoginFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            
            UserRepository userRepository = new UserRepository();
            ExpenseRepository expenseRepository = new ExpenseRepository();
            IncomeRepository incomeRepository = new IncomeRepository();
            CategoryRepository categoryRepository = new CategoryRepository();

            // ---------------- Services ----------------
            AuthService authService = new AuthService(userRepository);
            ExpenseService expenseService = new ExpenseService(expenseRepository);
            IncomeService incomeService = new IncomeService(incomeRepository);
            ReportService reportService = new ReportService(expenseService, incomeService);
            CategoryService categoryService = new CategoryService(categoryRepository);
            BudgetService budgetService = new BudgetService(expenseService, categoryService);

            // ---------------- Launch LoginFrame ----------------
            LoginFrame loginFrame = new LoginFrame(
                    authService,
                    expenseService,
                    incomeService,
                    reportService,
                    budgetService,
                    categoryService
            );

            loginFrame.setVisible(true);
        });
    }
}
