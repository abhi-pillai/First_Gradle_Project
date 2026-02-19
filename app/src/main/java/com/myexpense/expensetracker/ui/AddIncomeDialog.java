package com.myexpense.expensetracker.ui;

import com.myexpense.expensetracker.model.Income;
import com.myexpense.expensetracker.service.IncomeService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class AddIncomeDialog extends JDialog {

    public AddIncomeDialog(JFrame parent,
                           IncomeService incomeService,
                           Runnable onIncomeAdded) {

        super(parent, "Add Income", true);

        setSize(400, 250);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(5, 2, 10, 10));

        JTextField amountField = new JTextField();
        JTextField sourceField = new JTextField();
        JTextField noteField = new JTextField();

        add(new JLabel("Amount:"));
        add(amountField);

        add(new JLabel("Source:"));
        add(sourceField);

        add(new JLabel("Note:"));
        add(noteField);

        JButton saveBtn = new JButton("Save");
        add(new JLabel());
        add(saveBtn);

        saveBtn.addActionListener(e -> {

            try {
                double amount = Double.parseDouble(amountField.getText());
                String source = sourceField.getText();
                String note = noteField.getText();

                Income income = new Income(
                        amount,
                        LocalDate.now(),
                        source,
                        note
                );

                incomeService.addIncome(income);

                onIncomeAdded.run();  // callback to refresh dashboard
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid input!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
