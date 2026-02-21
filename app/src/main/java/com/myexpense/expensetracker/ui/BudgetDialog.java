package com.myexpense.expensetracker.ui;

import com.myexpense.expensetracker.model.Category;
import com.myexpense.expensetracker.service.BudgetService;
import com.myexpense.expensetracker.service.CategoryService;

import javax.swing.*;
import java.awt.*;
import java.time.YearMonth;
import java.util.List;

public class BudgetDialog extends JDialog {

    private final BudgetService   budgetService;
    private final CategoryService categoryService;
    private final String          userId;

    private JTextArea budgetArea;
    private JTextField budgetInput;

    public BudgetDialog(JFrame parent, BudgetService budgetService,
                        CategoryService categoryService, String userId) {
        super(parent, "Budget Overview", true);
        this.budgetService   = budgetService;
        this.categoryService = categoryService;
        this.userId          = userId;

        setSize(440, 480);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(8, 8));

        add(buildSetBudgetPanel(), BorderLayout.NORTH);
        add(buildBudgetDisplay(),  BorderLayout.CENTER);
        add(buildCloseBtn(),       BorderLayout.SOUTH);

        refreshDisplay();
    }

    private JPanel buildSetBudgetPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Set Monthly Budget"));

        double current = budgetService.getMonthlyBudget(userId);
        budgetInput = new JTextField(String.format("%.2f", current), 10);

        JButton setBtn = new JButton("Set Budget");
        setBtn.setBackground(new Color(41, 128, 185));
        setBtn.setFocusPainted(false);

        setBtn.addActionListener(e -> {
            try {
                double budget = Double.parseDouble(budgetInput.getText().trim());
                if (budget < 0) throw new NumberFormatException();
                budgetService.setMonthlyBudget(userId, budget);
                JOptionPane.showMessageDialog(this, "Budget updated!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                refreshDisplay();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid non-negative amount.", "Invalid", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JLabel("Monthly Budget (₹):"));
        panel.add(budgetInput);
        panel.add(setBtn);
        return panel;
    }

    private JScrollPane buildBudgetDisplay() {
        budgetArea = new JTextArea();
        budgetArea.setEditable(false);
        budgetArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        budgetArea.setMargin(new Insets(10, 14, 10, 14));
        return new JScrollPane(budgetArea);
    }

    private JPanel buildCloseBtn() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        p.add(close);
        return p;
    }

    private void refreshDisplay() {
        YearMonth thisMonth = YearMonth.now();
        double budget    = budgetService.getMonthlyBudget(userId);
        double remaining = budgetService.getRemainingMonthlyBudget(userId, thisMonth);
        boolean over     = budgetService.isMonthlyOverBudget(userId, thisMonth);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Month  : %s\n", thisMonth));
        sb.append(String.format("Budget : ₹%,.2f\n", budget));
        sb.append(String.format("Remaining: ₹%,.2f  %s\n\n",
                remaining, over ? "⚠ OVER BUDGET!" : "✔"));

        sb.append("─── Category Budgets (This Month) ───\n\n");

        List<Category> cats = categoryService.getAllCategories(userId);
        if (cats.isEmpty()) {
            sb.append("  No categories found.\n");
        } else {
            for (Category c : cats) {
                if (c.getMonthlyBudget() <= 0) continue; // skip unset
                try {
                    double catRemaining = budgetService.getRemainingCategoryBudget(
                            userId, c.getName(), thisMonth);
                    boolean catOver = catRemaining < 0;
                    sb.append(String.format("  %-18s Budget: ₹%,.0f  Rem: ₹%,.2f  %s\n",
                            c.getName(),
                            c.getMonthlyBudget(),
                            catRemaining,
                            catOver ? "⚠ OVER" : "✔"));
                } catch (Exception ignored) {}
            }
        }

        budgetArea.setText(sb.toString());
    }
}