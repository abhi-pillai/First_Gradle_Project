package com.myexpense.expensetracker.ui;

import com.myexpense.expensetracker.model.Expense;
import com.myexpense.expensetracker.model.PaymentMethod;
import com.myexpense.expensetracker.service.ExpenseService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class AddExpenseDialog extends JDialog {

    public AddExpenseDialog(JFrame parent,
                            ExpenseService expenseService,
                            Runnable onExpenseAdded) {

        super(parent, "Add Expense", true);

        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(6, 2, 10, 10));

        JTextField amountField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField noteField = new JTextField();

        JComboBox<PaymentMethod> paymentMethodBox =
                new JComboBox<>(PaymentMethod.values());

        add(new JLabel("Amount:"));
        add(amountField);

        add(new JLabel("Category:"));
        add(categoryField);

        add(new JLabel("Note:"));
        add(noteField);

        add(new JLabel("Payment Method:"));
        add(paymentMethodBox);

        JButton saveBtn = new JButton("Save");
        add(new JLabel());
        add(saveBtn);

        saveBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String category = categoryField.getText();
                String note = noteField.getText();
                PaymentMethod method =
                        (PaymentMethod) paymentMethodBox.getSelectedItem();

                Expense expense = new Expense(
                        amount,
                        LocalDate.now(),
                        category,
                        note,
                        method
                );

                expenseService.addExpense(expense);

                onExpenseAdded.run(); // refresh table
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
