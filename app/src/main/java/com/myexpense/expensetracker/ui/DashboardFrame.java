package com.myexpense.expensetracker.ui;

import com.myexpense.expensetracker.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DashboardFrame extends JFrame {

    private final AuthService authService;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final ReportService reportService;
    private final BudgetService budgetService;
    private final CategoryService categoryService;

    private JTable expenseTable;
    private DefaultTableModel tableModel;

    public DashboardFrame(AuthService authService,
                          ExpenseService expenseService,
                          IncomeService incomeService,
                          ReportService reportService,
                          BudgetService budgetService,
                          CategoryService categoryService) {

        this.authService = authService;
        this.expenseService = expenseService;
        this.incomeService = incomeService;
        this.reportService = reportService;
        this.budgetService = budgetService;
        this.categoryService = categoryService;

        setTitle("Expense Tracker - Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initializeUI();
        loadExpensesToTable();
        refreshDashboardSummary();
    }

    private void initializeUI() {

        setLayout(new BorderLayout());

        // ---------------- Top Panel ----------------
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(30, 144, 255));
        topPanel.setPreferredSize(new Dimension(800, 60));

        JLabel welcomeLabel = new JLabel("Welcome, " + authService.getLoggedInUser().getUsername());
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(welcomeLabel);

        add(topPanel, BorderLayout.NORTH);

        // ---------------- Left Navigation ----------------
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(6, 1, 5, 5));
        leftPanel.setPreferredSize(new Dimension(150, 0));

        // Add Expense Button
        JButton addExpenseBtn = new JButton("Add Expense");
        addExpenseBtn.addActionListener(e -> {
            AddExpenseDialog dialog = new AddExpenseDialog(this, expenseService, this::loadExpensesToTable);
            dialog.setVisible(true);
            refreshDashboardSummary();
        });

        // Add Income Button
        JButton addIncomeBtn = new JButton("Add Income");
        addIncomeBtn.addActionListener(e -> {
            AddIncomeDialog dialog = new AddIncomeDialog(this, incomeService, this::refreshDashboardSummary);
            dialog.setVisible(true);
            refreshDashboardSummary();
        });

        // Reports Button
        JButton reportsBtn = new JButton("Reports");
        reportsBtn.addActionListener(e -> {
            ReportsDialog dialog = new ReportsDialog(this, reportService);
            dialog.setVisible(true);
        });

        // Budget Button
        JButton budgetBtn = new JButton("Budget");
        budgetBtn.addActionListener(e -> {
            List<String> categories = categoryService.getAllCategories()
                    .stream()
                    .map(c -> c.getName())
                    .toList();

            BudgetDialog dialog = new BudgetDialog(this, budgetService, categories);
            dialog.setVisible(true);
        });

        // Logout Button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            authService.logout();
            new LoginFrame(
                    authService,
                    expenseService,
                    incomeService,
                    reportService,
                    budgetService,
                    categoryService
            ).setVisible(true);
            dispose();
        });

        leftPanel.add(addExpenseBtn);
        leftPanel.add(addIncomeBtn);
        leftPanel.add(reportsBtn);
        leftPanel.add(budgetBtn);
        leftPanel.add(logoutBtn);

        add(leftPanel, BorderLayout.WEST);

        // ---------------- Center Panel (Expenses Table) ----------------
        String[] columns = {"Date", "Category", "Amount", "Payment Method", "Note"};
        tableModel = new DefaultTableModel(columns, 0);
        expenseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(expenseTable);

        add(scrollPane, BorderLayout.CENTER);
    }

    // ---------------- Load Expenses ----------------
    private void loadExpensesToTable() {
        tableModel.setRowCount(0); // clear old rows
        expenseService.getAllExpenses().forEach(exp -> tableModel.addRow(new Object[]{
                exp.getDate(),
                exp.getCategory(),
                exp.getAmount(),
                exp.getPaymentMethod(),
                exp.getNote()
        }));
    }

    // ---------------- Dashboard Summary ----------------
    private void refreshDashboardSummary() {
        double totalIncome = incomeService.getTotalIncome();
        double totalExpense = expenseService.getTotalExpenses();
        double netSavings = totalIncome - totalExpense;

        System.out.println("Dashboard Summary:");
        System.out.println("Total Income: " + totalIncome);
        System.out.println("Total Expenses: " + totalExpense);
        System.out.println("Net Savings: " + netSavings);
    }
}
