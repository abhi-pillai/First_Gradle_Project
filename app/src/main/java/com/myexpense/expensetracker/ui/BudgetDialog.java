package com.myexpense.expensetracker.ui;

import com.myexpense.expensetracker.service.BudgetService;

import javax.swing.*;
import java.awt.*;
import java.time.YearMonth;
import java.util.List;

public class BudgetDialog extends JDialog {

    public BudgetDialog(JFrame parent, BudgetService budgetService, List<String> categories) {
        super(parent, "Budget Overview", true);

        setSize(400, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JTextArea budgetArea = new JTextArea();
        budgetArea.setEditable(false);
        budgetArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        YearMonth thisMonth = YearMonth.now();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Monthly Budget: %.2f\n", budgetService.getMonthlyBudget()));
        sb.append(String.format("Remaining This Month: %.2f\n\n", budgetService.getRemainingMonthlyBudget(thisMonth)));

        sb.append("Category Budgets:\n");
        sb.append("--------------------\n");

        for (String cat : categories) {
            double remaining = budgetService.getRemainingCategoryBudget(cat);
            sb.append(String.format("%-15s : %.2f %s\n",
                    cat,
                    remaining,
                    remaining < 0 ? "(Over Budget!)" : ""));
        }

        budgetArea.setText(sb.toString());

        add(new JScrollPane(budgetArea), BorderLayout.CENTER);
    }
}
