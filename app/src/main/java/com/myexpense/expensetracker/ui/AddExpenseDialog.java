package com.myexpense.expensetracker.ui;

import com.myexpense.expensetracker.model.Expense;
import com.myexpense.expensetracker.model.PaymentMethod;
import com.myexpense.expensetracker.service.CategoryService;
import com.myexpense.expensetracker.service.ExpenseService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class AddExpenseDialog extends JDialog {

    public AddExpenseDialog(JFrame parent, ExpenseService expenseService,
                            CategoryService categoryService, String userId,
                            Runnable onDone) {
        super(parent, "Add Expense", true);
        setSize(420, 340);
        setLocationRelativeTo(parent);

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JTextField amountField = new JTextField();

        // Date spinner
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new Date());

        // Category dropdown from service
        List<String> catNames = categoryService.getCategoryNames(userId);
        JComboBox<String> categoryBox = new JComboBox<>(catNames.toArray(new String[0]));

        JTextField noteField    = new JTextField();
        JComboBox<PaymentMethod> paymentBox = new JComboBox<>(PaymentMethod.values());

        form.add(new JLabel("Amount (₹):")); form.add(amountField);
        form.add(new JLabel("Date:"));       form.add(dateSpinner);
        form.add(new JLabel("Category:"));   form.add(categoryBox);
        form.add(new JLabel("Note:"));       form.add(noteField);
        form.add(new JLabel("Payment:"));    form.add(paymentBox);
        form.add(new JLabel());

        JButton saveBtn = new JButton("Save Expense");
        saveBtn.setBackground(new Color(192, 57, 43));
        saveBtn.setFocusPainted(false);
        form.add(saveBtn);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);

        saveBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) throw new NumberFormatException();

                Date d = (Date) dateSpinner.getValue();
                LocalDate date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                String category = (String) categoryBox.getSelectedItem();
                String note     = noteField.getText().trim();
                PaymentMethod pm = (PaymentMethod) paymentBox.getSelectedItem();

                Expense expense = new Expense(userId, amount, date, category, note, pm);
                expenseService.addExpense(expense);
                onDone.run();
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid positive amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving expense: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}