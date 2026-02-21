package com.myexpense.expensetracker.ui;

import com.myexpense.expensetracker.model.Income;
import com.myexpense.expensetracker.service.IncomeService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class AddIncomeDialog extends JDialog {

    public AddIncomeDialog(JFrame parent, IncomeService incomeService,
                           String userId, Runnable onDone) {
        super(parent, "Add Income", true);
        setSize(400, 290);
        setLocationRelativeTo(parent);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JTextField amountField = new JTextField();
        JTextField sourceField = new JTextField();
        JTextField noteField   = new JTextField();

        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new Date());

        form.add(new JLabel("Amount (₹):")); form.add(amountField);
        form.add(new JLabel("Date:"));       form.add(dateSpinner);
        form.add(new JLabel("Source:"));     form.add(sourceField);
        form.add(new JLabel("Note:"));       form.add(noteField);
        form.add(new JLabel());

        JButton saveBtn = new JButton("Save Income");
        saveBtn.setBackground(new Color(39, 174, 96));
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

                String source = sourceField.getText().trim();
                String note   = noteField.getText().trim();
                if (source.isEmpty()) { JOptionPane.showMessageDialog(this, "Source cannot be empty."); return; }

                incomeService.addIncome(new Income(userId, amount, date, source, note));
                onDone.run();
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid positive amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}