package com.myexpense.expensetracker.ui;

import com.myexpense.expensetracker.model.Category;
import com.myexpense.expensetracker.service.CategoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageCategoriesDialog extends JDialog {

    private final CategoryService categoryService;
    private final String userId;
    private final Runnable onChanged;

    private DefaultTableModel tableModel;
    private JTable table;
    private List<Category> categories;

    public ManageCategoriesDialog(JFrame parent, CategoryService categoryService,
                                   String userId, Runnable onChanged) {
        super(parent, "Manage Categories", true);
        this.categoryService = categoryService;
        this.userId          = userId;
        this.onChanged       = onChanged;

        setSize(500, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(8, 8));

        add(buildTable(),   BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);
        loadData();
    }

    private JScrollPane buildTable() {
        String[] cols = {"Category Name", "Monthly Budget (₹)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return new JScrollPane(table);
    }

    private JPanel buildActions() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));

        JButton addBtn    = new JButton("➕ Add Category");
        JButton editBtn   = new JButton("✏ Edit Budget");
        JButton deleteBtn = new JButton("🗑 Delete");
        JButton closeBtn  = new JButton("Close");

        addBtn.setBackground(new Color(39, 174, 96));
        addBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        deleteBtn.setForeground(new Color(192, 57, 43));
        deleteBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        editBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));


        addBtn.addActionListener(e    -> addCategory());
        editBtn.addActionListener(e   -> editSelectedBudget());
        deleteBtn.addActionListener(e -> deleteSelected());
        closeBtn.addActionListener(e  -> dispose());

        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(deleteBtn);
        panel.add(closeBtn);
        return panel;
    }

    private void loadData() {
        categories = categoryService.getAllCategories(userId);
        tableModel.setRowCount(0);
        for (Category c : categories) {
            tableModel.addRow(new Object[]{c.getName(), String.format("%.2f", c.getMonthlyBudget())});
        }
    }

    private void addCategory() {
        JTextField nameField   = new JTextField();
        JTextField budgetField = new JTextField("0");

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Category Name:")); form.add(nameField);
        form.add(new JLabel("Monthly Budget:")); form.add(budgetField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add Category",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "Name cannot be empty."); return; }

        double budget;
        try { budget = Double.parseDouble(budgetField.getText().trim()); }
        catch (NumberFormatException ex) { budget = 0.0; }

        try {
            categoryService.addCategory(userId, new Category(userId, name, budget));
            onChanged.run();
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void editSelectedBudget() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a category first."); return; }

        Category cat = categories.get(row);
        String input = JOptionPane.showInputDialog(this,
                "New monthly budget for \"" + cat.getName() + "\":",
                String.format("%.2f", cat.getMonthlyBudget()));
        if (input == null) return;

        try {
            double budget = Double.parseDouble(input.trim());
            cat.setMonthlyBudget(budget);
            categoryService.updateCategory(userId, cat);
            onChanged.run();
            loadData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a category first."); return; }

        Category cat = categories.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete category \"" + cat.getName() + "\"?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            categoryService.deleteCategory(userId, cat.getId());
            onChanged.run();
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}