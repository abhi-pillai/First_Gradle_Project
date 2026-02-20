package com.myexpense.expensetracker.ui;

import com.myexpense.expensetracker.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private final AuthService     authService;
    private final ExpenseService  expenseService;
    private final IncomeService   incomeService;
    private final ReportService   reportService;
    private final BudgetService   budgetService;
    private final CategoryService categoryService;

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         strengthLabel;

    private static final Color BRAND_BLUE  = new Color(30, 80, 160);
    private static final Color BRAND_GREEN = new Color(39, 174, 96);
    private static final Color CARD_BG     = new Color(250, 251, 255);
    private static final Color LABEL_COLOR = new Color(60, 60, 80);
    private static final Color SUBTLE_TEXT = new Color(140, 150, 170);

    public LoginFrame(AuthService authService, ExpenseService expenseService,
                      IncomeService incomeService, ReportService reportService,
                      BudgetService budgetService, CategoryService categoryService) {
        this.authService     = authService;
        this.expenseService  = expenseService;
        this.incomeService   = incomeService;
        this.reportService   = reportService;
        this.budgetService   = budgetService;
        this.categoryService = categoryService;

        setTitle("TrackIt - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(360, 540));
        setPreferredSize(new Dimension(420, 580));
        pack();
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        // ── Gradient background root panel ───────────────────
        JPanel root = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0,          new Color(20, 60, 140),
                        0, getHeight(), new Color(50, 110, 200));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets  = new Insets(0, 32, 0, 32);

        // ── App title ─────────────────────────────────────────
        JLabel appTitle = new JLabel("TrackIt", SwingConstants.CENTER);
        appTitle.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 42));
        appTitle.setForeground(Color.WHITE);
        appTitle.setBorder(new EmptyBorder(36, 0, 4, 0));
        gbc.gridy = 0;
        root.add(appTitle, gbc);

        // ── Tagline ───────────────────────────────────────────
        JLabel tagline = new JLabel("Track your money, own your day.", SwingConstants.CENTER);
        tagline.setFont(new Font("Arial", Font.ITALIC, 12));
        tagline.setForeground(new Color(180, 210, 255));
        tagline.setBorder(new EmptyBorder(0, 0, 24, 0));
        gbc.gridy = 1;
        root.add(tagline, gbc);

        // ── Card ──────────────────────────────────────────────
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Shared constraints for card children
        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        // Row 0 — Username label
        c.gridy = 0; c.insets = new Insets(0, 0, 5, 0);
        card.add(fieldLabel("Username"), c);

        // Row 1 — Username field
        usernameField = styledTextField("Enter username");
        c.gridy = 1; c.insets = new Insets(0, 0, 16, 0);
        card.add(usernameField, c);

        // Row 2 — Password label
        c.gridy = 2; c.insets = new Insets(0, 0, 5, 0);
        card.add(fieldLabel("Password"), c);

        // Row 3 — Password field + eye toggle button in an overlay panel
        passwordField = styledPasswordField("Enter password");
        JPanel passwordRow = new JPanel(new BorderLayout(0, 0));
        passwordRow.setOpaque(false);
        passwordRow.add(passwordField, BorderLayout.CENTER);
        passwordRow.add(buildEyeButton(), BorderLayout.EAST);
        c.gridy = 3; c.insets = new Insets(0, 0, 6, 0);
        card.add(passwordRow, c);

        // Row 4 — Strength indicator (fixed gridy, no overlap)
        strengthLabel = new JLabel(" ");
        strengthLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        strengthLabel.setForeground(SUBTLE_TEXT);
        c.gridy = 4; c.insets = new Insets(0, 2, 20, 0);
        card.add(strengthLabel, c);

        // Row 5 — Login button
        JButton loginBtn = styledButton("Login", BRAND_BLUE, Color.WHITE);
        c.gridy = 5; c.insets = new Insets(0, 0, 10, 0);
        card.add(loginBtn, c);

        // Row 6 — Register button
        JButton registerBtn = styledButton("Register", BRAND_GREEN, Color.WHITE);
        c.gridy = 6; c.insets = new Insets(0, 0, 0, 0);
        card.add(registerBtn, c);

        gbc.gridy = 2; gbc.insets = new Insets(0, 32, 0, 32);
        root.add(card, gbc);

        // ── Footer ────────────────────────────────────────────
        JLabel footer = new JLabel("TrackIt v1.0", SwingConstants.CENTER);
        footer.setFont(new Font("Arial", Font.PLAIN, 10));
        footer.setForeground(new Color(140, 170, 220));
        footer.setBorder(new EmptyBorder(14, 0, 8, 0));
        gbc.gridy = 3;
        root.add(footer, gbc);

        setContentPane(root);

        // ── Wire up actions ───────────────────────────────────
        loginBtn.addActionListener(e    -> login());
        registerBtn.addActionListener(e -> register());
        passwordField.addActionListener(e -> login());
        usernameField.addActionListener(e -> passwordField.requestFocus());

        // ── Live password strength listener ───────────────────
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { updateStrength(); }
            public void removeUpdate(DocumentEvent e)  { updateStrength(); }
            public void changedUpdate(DocumentEvent e) { updateStrength(); }
        });
    }

    // ════════════════════════════════════════════════════════
    //  Password strength meter
    // ════════════════════════════════════════════════════════
    private void updateStrength() {
        String pwd = new String(passwordField.getPassword());

        // Don't show hint if showing placeholder
        if (pwd.equals("Enter password") || pwd.isEmpty()) {
            strengthLabel.setText(" ");
            return;
        }

        int score = 0;
        if (pwd.length() >= 8)                                              score++;
        if (pwd.matches(".*[A-Z].*"))                                       score++;
        if (pwd.matches(".*[a-z].*"))                                       score++;
        if (pwd.matches(".*[0-9].*"))                                       score++;
        if (pwd.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?].*"))    score++;

        String hint;
        Color  color;
        if      (score <= 1) { hint = "Weak";        color = new Color(192, 57, 43);  }
        else if (score <= 3) { hint = "Medium";      color = new Color(230, 126, 34); }
        else if (score == 4) { hint = "Strong";      color = new Color(39, 174, 96);  }
        else                 { hint = "Very Strong"; color = new Color(26, 140, 80);  }

        strengthLabel.setText("Password strength: " + hint);
        strengthLabel.setForeground(color);
    }

    // ════════════════════════════════════════════════════════
    //  Styled component helpers
    // ════════════════════════════════════════════════════════
    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(LABEL_COLOR);
        return lbl;
    }

    private JTextField styledTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? BRAND_BLUE : new Color(200, 210, 230));
                g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };
        field.setOpaque(false);
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setForeground(SUBTLE_TEXT);
        field.setBorder(new EmptyBorder(10, 12, 10, 12));
        field.setPreferredSize(new Dimension(0, 42));
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(LABEL_COLOR);
                }
                field.repaint();
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(SUBTLE_TEXT);
                }
                field.repaint();
            }
        });
        return field;
    }

    private JPasswordField styledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? BRAND_BLUE : new Color(200, 210, 230));
                g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };
        field.setOpaque(false);
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setForeground(SUBTLE_TEXT);
        field.setBorder(new EmptyBorder(10, 12, 10, 12));
        field.setPreferredSize(new Dimension(0, 42));
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setForeground(LABEL_COLOR);
                    // ── FIX: respect the current passwordVisible state ──
                    field.setEchoChar(passwordVisible ? (char) 0 : '*');
                }
                field.repaint();
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0);
                    field.setText(placeholder);
                    field.setForeground(SUBTLE_TEXT);
                }
                field.repaint();
            }
        });
        return field;
    }

    private boolean passwordVisible = false;

    private JButton buildEyeButton() {
        JButton eye = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background — matches password field
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Right border to blend with field
                g2.setColor(passwordField.isFocusOwner() ? BRAND_BLUE : new Color(200, 210, 230));
                g2.setStroke(new BasicStroke(passwordField.isFocusOwner() ? 2f : 1f));
                g2.drawLine(0, 0, 0, getHeight()); // left divider

                // Draw eye icon manually
                int cx = getWidth()  / 2;
                int cy = getHeight() / 2;
                g2.setColor(new Color(120, 130, 150));
                g2.setStroke(new BasicStroke(1.6f));

                if (passwordVisible) {
                    // Open eye: arc + pupil
                    g2.drawArc(cx - 8, cy - 5, 16, 10, 0, 180);
                    g2.drawArc(cx - 8, cy - 5, 16, 10, 0, -180);
                    g2.fillOval(cx - 3, cy - 3, 6, 6);
                } else {
                    // Closed eye: arc + strike-through line
                    g2.drawArc(cx - 8, cy - 4, 16, 10, 0, 180);
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawLine(cx - 9, cy + 4, cx + 9, cy - 5);
                }
            }
        };

        eye.setPreferredSize(new Dimension(42, 42));
        eye.setOpaque(false);
        eye.setContentAreaFilled(false);
        eye.setBorderPainted(false);
        eye.setFocusPainted(false);
        eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eye.setToolTipText("Show/hide password");

        eye.addActionListener(e -> {
            String current = new String(passwordField.getPassword());
            boolean isPlaceholder = current.equals("Enter password");

            passwordVisible = !passwordVisible;

            // ── FIX: always update echo char regardless of placeholder state.
            // If placeholder is showing we still track the toggle so that when
            // the user starts typing, focusGained will apply the correct echo char.
            if (!isPlaceholder) {
                passwordField.setEchoChar(passwordVisible ? (char) 0 : '*');
            }

            eye.repaint();
            passwordField.repaint();
            passwordField.requestFocus();
        });

        // Repaint eye when password field focus changes (border color sync)
        passwordField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { eye.repaint(); }
            @Override public void focusLost(FocusEvent e)   { eye.repaint(); }
        });

        return eye;
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? bg.darker()
                           : getModel().isRollover() ? bg.brighter()
                           : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 44));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return btn;
    }

    // ════════════════════════════════════════════════════════
    //  Business logic
    // ════════════════════════════════════════════════════════
    private String getUsername() {
        String t = usernameField.getText().trim();
        return t.equals("Enter username") ? "" : t;
    }

    private String getPassword() {
        String t = new String(passwordField.getPassword()).trim();
        return t.equals("Enter password") ? "" : t;
    }

    private void login() {
        String username = getUsername();
        String password = getPassword();
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (authService.login(username, password)) {
            showAutoCloseSuccess();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            resetPasswordField();
        }
    }

    private void register() {
        String username = getUsername();
        String password = getPassword();
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String strengthError = validatePassword(password);
        if (strengthError != null) {
            JOptionPane.showMessageDialog(this, strengthError,
                    "Weak Password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            authService.register(username, password);
            JOptionPane.showMessageDialog(this,
                    "Registered successfully! Default categories added.\nYou can now log in.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            usernameField.setText("Enter username");
            usernameField.setForeground(SUBTLE_TEXT);
            resetPasswordField();
            usernameField.requestFocus();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String validatePassword(String password) {
        if (password.length() < 8)
            return "Password must be at least 8 characters.";
        if (!password.matches(".*[A-Z].*"))
            return "Password must contain at least one uppercase letter (A-Z).";
        if (!password.matches(".*[a-z].*"))
            return "Password must contain at least one lowercase letter (a-z).";
        if (!password.matches(".*[0-9].*"))
            return "Password must contain at least one number (0-9).";
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?].*"))
            return "Password must contain at least one special character (!@#$%^&* etc).";
        if (password.matches(".*(.)\\1{2,}.*"))
            return "Password must not have 3 or more repeating characters (e.g. aaa, 111).";
        return null;
    }

    private void resetPasswordField() {
        passwordVisible = false;
        passwordField.setEchoChar((char) 0);
        passwordField.setText("Enter password");
        passwordField.setForeground(SUBTLE_TEXT);
        strengthLabel.setText(" ");
    }

    private void showAutoCloseSuccess() {
        JDialog dialog = new JDialog(this, "Welcome!", true);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BRAND_GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lbl = new JLabel("Login Successful!", SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 15));
        panel.add(lbl);

        dialog.add(panel);
        dialog.setSize(250, 75);
        dialog.setLocationRelativeTo(this);
        dialog.getRootPane().setBorder(
                BorderFactory.createLineBorder(new Color(0,0,0,0), 2));

        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
            dialog.dispose();
            new DashboardFrame(authService, expenseService, incomeService,
                    reportService, budgetService, categoryService).setVisible(true);
            dispose();
        });
        timer.setRepeats(false);
        timer.start();
        dialog.setVisible(true);
    }
}