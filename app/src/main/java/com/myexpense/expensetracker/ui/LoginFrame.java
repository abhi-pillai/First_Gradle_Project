package com.myexpense.expensetracker.ui;

import com.myexpense.expensetracker.service.*;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final AuthService authService;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final ReportService reportService;
    private final BudgetService budgetService;
    private final CategoryService categoryService;

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame(AuthService authService,
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

        setTitle("TrackIt - Login");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initializeUI();
    }

    private void initializeUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        panel.add(loginButton);
        panel.add(registerButton);

        add(panel);

        // Button actions
        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username and Password cannot be empty!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = authService.login(username, password);

        if (success) {
            showAutoCloseDialog("Login Successful!", 1000);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username and Password cannot be empty!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            authService.register(username, password);

            JOptionPane.showMessageDialog(this,
                    "User registered successfully!");

            usernameField.setText("");
            passwordField.setText("");
            usernameField.requestFocus();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAutoCloseDialog(String message, int durationMillis) {
        final JDialog dialog = new JDialog(this, "Success", true);
        dialog.setLayout(new BorderLayout());

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(new Color(34, 139, 34));
        dialog.add(label, BorderLayout.CENTER);

        dialog.setSize(250, 120);
        dialog.setLocationRelativeTo(this);

        javax.swing.Timer timer = new javax.swing.Timer(durationMillis, e -> {
            dialog.dispose();

            // Open dashboard with all required services
            DashboardFrame dashboard = new DashboardFrame(
                    authService,
                    expenseService,
                    incomeService,
                    reportService,
                    budgetService,
                    categoryService
            );
            dashboard.setVisible(true);

            this.dispose();
        });

        timer.setRepeats(false);  // run only once
        timer.start();

        dialog.setVisible(true);
    }
}
