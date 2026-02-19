package com.myexpense.expensetracker.ui;

import com.myexpense.expensetracker.service.ReportService;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ReportsDialog extends JDialog {

    public ReportsDialog(JFrame parent, ReportService reportService) {
        super(parent, "Reports", true);

        setSize(400, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        // ===========================
        // Generate report text
        // ===========================
        StringBuilder sb = new StringBuilder();

        sb.append("Income vs Expenses Summary:\n");
        sb.append("----------------------------\n");
        sb.append(String.format("Total Income: %.2f\n", reportService.getTotalIncome()));
        sb.append(String.format("Total Expenses: %.2f\n", reportService.getTotalExpenses()));
        sb.append(String.format("Net Savings: %.2f\n\n", reportService.getNetSavings()));

        sb.append("Category Breakdown:\n");
        sb.append("----------------------------\n");

        Map<String, Double> breakdown = reportService.getCategoryBreakdown();
        breakdown.forEach((cat, amt) -> {
            sb.append(String.format("%-15s : %.2f\n", cat, amt));
        });

        reportArea.setText(sb.toString());

        add(new JScrollPane(reportArea), BorderLayout.CENTER);
    }
}
