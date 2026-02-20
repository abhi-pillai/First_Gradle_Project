package com.myexpense.expensetracker.ui;

import com.myexpense.expensetracker.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class AccountSettingsDialog extends JDialog {

    private final AuthService authService;
    private final Runnable    onAccountDeleted;

    private static final Color BRAND_BLUE  = new Color(30, 80, 160);
    private static final Color DANGER_RED  = new Color(192, 57, 43);
    private static final Color LABEL_COLOR = new Color(50, 50, 70);
    private static final Color SUBTLE_TEXT = new Color(140, 150, 170);

    // ── Fixed dialog dimensions ────────────────────────────────
    private static final int DIALOG_WIDTH  = 460;
    private static final int DIALOG_HEIGHT = 580;

    // ── Per-field eye-toggle visibility flags ─────────────────
    private boolean visibleCurrent = false;
    private boolean visibleNew     = false;
    private boolean visibleConfirm = false;
    private boolean visibleDelete  = false;

    public AccountSettingsDialog(JFrame parent, AuthService authService,
                                  Runnable onAccountDeleted) {
        super(parent, "Account Settings", true);
        this.authService      = authService;
        this.onAccountDeleted = onAccountDeleted;

        // ── Fixed size, not resizable ──────────────────────────
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setResizable(false);
        setLocationRelativeTo(parent);
        buildUI();
    }

    // ════════════════════════════════════════════════════════
    //  UI BUILD
    // ════════════════════════════════════════════════════════
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 252));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    // ── Header — fixed height ──────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 60, 140),
                        getWidth(), 0, new Color(35, 90, 175));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(DIALOG_WIDTH, 68));
        header.setMinimumSize(new Dimension(0, 68));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Account Settings");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Logged in as: " + authService.getLoggedInUser().getUsername());
        sub.setFont(new Font("Arial", Font.PLAIN, 11));
        sub.setForeground(new Color(180, 210, 255));

        JPanel text = new JPanel(new BorderLayout(0, 3));
        text.setOpaque(false);
        text.add(title, BorderLayout.NORTH);
        text.add(sub,   BorderLayout.SOUTH);
        header.add(text, BorderLayout.CENTER);
        return header;
    }

    // ── Body — scrollable, fills all remaining space ──────────
    private JScrollPane buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(new Color(245, 247, 252));
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        body.add(buildChangePasswordSection());
        body.add(Box.createVerticalStrut(20));
        body.add(buildDangerZoneSection());
        // Glue pushes sections to top when content is shorter than viewport
        body.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        // Never show horizontal scrollbar — content must fit within fixed width
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Tell the scroll pane's viewport to track the width of the dialog
        scroll.getViewport().addChangeListener(e -> {
            int vpWidth = scroll.getViewport().getWidth();
            if (body.getPreferredSize().width != vpWidth) {
                body.setPreferredSize(new Dimension(vpWidth, body.getPreferredSize().height));
                body.revalidate();
            }
        });

        return scroll;
    }

    // ── Footer — fixed height ──────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        footer.setBackground(new Color(235, 238, 245));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        footer.setPreferredSize(new Dimension(DIALOG_WIDTH, 50));
        footer.setMinimumSize(new Dimension(0, 50));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        JButton closeBtn = new JButton("Close");
        closeBtn.setPreferredSize(new Dimension(90, 32));
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);
        return footer;
    }

    // ════════════════════════════════════════════════════════
    //  Change Password Section
    // ════════════════════════════════════════════════════════
    private JPanel buildChangePasswordSection() {
        JPanel section = sectionPanel("Change Password");

        JPasswordField currentField = passwordField("Current password");
        JPasswordField newField     = passwordField("New password (min 8 chars)");
        JPasswordField confirmField = passwordField("Confirm new password");

        JPanel currentRow = withEye(currentField, "Current password",
                () -> visibleCurrent, v -> visibleCurrent = v);
        JPanel newRow     = withEye(newField,     "New password (min 8 chars)",
                () -> visibleNew,     v -> visibleNew     = v);
        JPanel confirmRow = withEye(confirmField, "Confirm new password",
                () -> visibleConfirm, v -> visibleConfirm = v);

        // Strength label
        JLabel strengthLbl = new JLabel(" ");
        strengthLbl.setFont(new Font("Arial", Font.ITALIC, 11));
        strengthLbl.setForeground(SUBTLE_TEXT);
        strengthLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        newField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            void update() {
                String pwd = new String(newField.getPassword());
                if (pwd.isEmpty() || pwd.equals("New password (min 8 chars)")) {
                    strengthLbl.setText(" "); return;
                }
                int score = 0;
                if (pwd.length() >= 8)                                           score++;
                if (pwd.matches(".*[A-Z].*"))                                    score++;
                if (pwd.matches(".*[a-z].*"))                                    score++;
                if (pwd.matches(".*[0-9].*"))                                    score++;
                if (pwd.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?].*")) score++;
                String hint; Color col;
                if      (score <= 1) { hint = "Weak";        col = DANGER_RED;             }
                else if (score <= 3) { hint = "Medium";      col = new Color(230, 126, 34); }
                else if (score == 4) { hint = "Strong";      col = new Color(39,  174, 96); }
                else                 { hint = "Very Strong"; col = new Color(26,  140, 80); }
                strengthLbl.setText("Strength: " + hint);
                strengthLbl.setForeground(col);
            }
        });

        JButton changeBtn = actionButton("Change Password", BRAND_BLUE);
        changeBtn.addActionListener(e -> {
            String current = fieldValue(currentField, "Current password");
            String newPwd  = fieldValue(newField,     "New password (min 8 chars)");
            String confirm = fieldValue(confirmField, "Confirm new password");

            if (current.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
                error("All fields are required."); return;
            }
            if (!newPwd.equals(confirm)) {
                error("New password and confirmation do not match."); return;
            }
            String weak = validatePassword(newPwd);
            if (weak != null) { error(weak); return; }

            try {
                authService.changePassword(current, newPwd);
                success("Password changed successfully!");
                resetField(currentField, "Current password");
                resetField(newField,     "New password (min 8 chars)");
                resetField(confirmField, "Confirm new password");
                visibleCurrent = false;
                visibleNew     = false;
                visibleConfirm = false;
                strengthLbl.setText(" ");
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });

        section.add(formRow("Current Password", currentRow));
        section.add(Box.createVerticalStrut(10));
        section.add(formRow("New Password",     newRow));
        section.add(strengthLbl);
        section.add(Box.createVerticalStrut(6));
        section.add(formRow("Confirm Password", confirmRow));
        section.add(Box.createVerticalStrut(14));
        section.add(changeBtn);
        return section;
    }

    // ════════════════════════════════════════════════════════
    //  Danger Zone Section
    // ════════════════════════════════════════════════════════
    private JPanel buildDangerZoneSection() {
        JPanel section = sectionPanel("Danger Zone");
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(220, 80, 60), 1),
                        "Danger Zone",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 12),
                        DANGER_RED),
                new EmptyBorder(10, 12, 14, 12)));

        // Bold first line
        JLabel boldWarning = new JLabel(
                "<html>Deleting your account is <b>permanent and irreversible</b>.</html>");
        boldWarning.setFont(new Font("Arial", Font.PLAIN, 12));
        boldWarning.setForeground(new Color(100, 30, 20));
        boldWarning.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Body text — JTextArea wraps naturally to whatever width the section has at runtime,
        // no hardcoded pixel math needed.
        JTextArea warningBody = new JTextArea(
                "All your expenses, income, categories and budget settings will be " +
                "erased immediately. You will be logged out.");
        warningBody.setFont(new Font("Arial", Font.PLAIN, 12));
        warningBody.setForeground(new Color(100, 30, 20));
        warningBody.setEditable(false);
        warningBody.setFocusable(false);
        warningBody.setLineWrap(true);
        warningBody.setWrapStyleWord(true);
        warningBody.setOpaque(false);
        warningBody.setAlignmentX(Component.LEFT_ALIGNMENT);
        warningBody.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPasswordField confirmPwd = passwordField("Enter your password to confirm");
        JPanel confirmRow = withEye(confirmPwd, "Enter your password to confirm",
                () -> visibleDelete, v -> visibleDelete = v);

        JButton deleteBtn = actionButton("Delete My Account", DANGER_RED);
        deleteBtn.addActionListener(e -> {
            String pwd = fieldValue(confirmPwd, "Enter your password to confirm");
            if (pwd.isEmpty()) { error("Please enter your password to confirm."); return; }

            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you absolutely sure?\n\nThis will permanently delete your account and all data.",
                    "Confirm Account Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;

            try {
                authService.deleteAccount(pwd);
                JOptionPane.showMessageDialog(this, "Your account has been deleted.",
                        "Account Deleted", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                onAccountDeleted.run();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });

        section.add(boldWarning);
        section.add(Box.createVerticalStrut(2));
        section.add(warningBody);
        section.add(Box.createVerticalStrut(12));
        section.add(formRow("Password", confirmRow));
        section.add(Box.createVerticalStrut(12));
        section.add(deleteBtn);
        return section;
    }

    // ════════════════════════════════════════════════════════
    //  Password field + eye button
    // ════════════════════════════════════════════════════════
    private JPasswordField passwordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? BRAND_BLUE : new Color(200, 210, 230));
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
            }
        };
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(0, 36));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setEchoChar((char) 0);
        field.setForeground(SUBTLE_TEXT);
        field.setText(placeholder);
        field.setBorder(new EmptyBorder(4, 8, 4, 8));

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0);
                    field.setText(placeholder);
                    field.setForeground(SUBTLE_TEXT);
                }
            }
        });
        return field;
    }

    private JPanel withEye(JPasswordField field, String placeholder,
                            BooleanSupplier getVisible, Consumer<Boolean> setVisible) {
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setForeground(LABEL_COLOR);
                    field.setEchoChar(getVisible.getAsBoolean() ? (char) 0 : '*');
                }
                field.repaint();
            }
        });

        JButton eye = buildEyeButton(field, placeholder, getVisible, setVisible);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.add(field, BorderLayout.CENTER);
        row.add(eye,   BorderLayout.EAST);
        return row;
    }

    private JButton buildEyeButton(JPasswordField field, String placeholder,
                                    BooleanSupplier getVisible, Consumer<Boolean> setVisible) {
        JButton eye = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(field.isFocusOwner() ? BRAND_BLUE : new Color(200, 210, 230));
                g2.setStroke(new BasicStroke(field.isFocusOwner() ? 1.5f : 1f));
                g2.drawLine(0, 0, 0, getHeight());

                int cx = getWidth()  / 2;
                int cy = getHeight() / 2;
                g2.setColor(new Color(120, 130, 150));
                g2.setStroke(new BasicStroke(1.6f));

                if (getVisible.getAsBoolean()) {
                    g2.drawArc(cx - 8, cy - 5, 16, 10, 0,  180);
                    g2.drawArc(cx - 8, cy - 5, 16, 10, 0, -180);
                    g2.fillOval(cx - 3, cy - 3, 6, 6);
                } else {
                    g2.drawArc(cx - 8, cy - 4, 16, 10, 0, 180);
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawLine(cx - 9, cy + 4, cx + 9, cy - 5);
                }
            }
        };

        eye.setPreferredSize(new Dimension(36, 36));
        eye.setOpaque(false);
        eye.setContentAreaFilled(false);
        eye.setBorderPainted(false);
        eye.setFocusPainted(false);
        eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eye.setToolTipText("Show/hide password");

        eye.addActionListener(e -> {
            boolean isPlaceholder = String.valueOf(field.getPassword()).equals(placeholder);
            setVisible.accept(!getVisible.getAsBoolean());
            if (!isPlaceholder) {
                field.setEchoChar(getVisible.getAsBoolean() ? (char) 0 : '*');
            }
            eye.repaint();
            field.repaint();
            field.requestFocus();
        });

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { eye.repaint(); }
            @Override public void focusLost(FocusEvent e)   { eye.repaint(); }
        });

        return eye;
    }

    // ════════════════════════════════════════════════════════
    //  Layout helpers
    // ════════════════════════════════════════════════════════

    /**
     * Section card — stretches to fill the scroll viewport width.
     */
    private JPanel sectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 210, 230), 1),
                        title,
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 12),
                        LABEL_COLOR),
                new EmptyBorder(10, 12, 14, 12)));
        return panel;
    }

    /**
     * Form row (label + field) — fills available width, capped height.
     */
    private JPanel formRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 11));
        lbl.setForeground(LABEL_COLOR);

        row.add(lbl,   BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    /**
     * Action button — full-width, fixed 40px height.
     */
    private JButton actionButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? bg.darker()
                           : getModel().isRollover() ? bg.brighter()
                           : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(0, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ════════════════════════════════════════════════════════
    //  Utility helpers
    // ════════════════════════════════════════════════════════
    private String fieldValue(JPasswordField field, String placeholder) {
        String val = new String(field.getPassword()).trim();
        return val.equals(placeholder) ? "" : val;
    }

    private void resetField(JPasswordField field, String placeholder) {
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.setForeground(SUBTLE_TEXT);
    }

    private String validatePassword(String pwd) {
        if (pwd.length() < 8)
            return "Password must be at least 8 characters.";
        if (!pwd.matches(".*[A-Z].*"))
            return "Password must contain at least one uppercase letter.";
        if (!pwd.matches(".*[a-z].*"))
            return "Password must contain at least one lowercase letter.";
        if (!pwd.matches(".*[0-9].*"))
            return "Password must contain at least one number.";
        if (!pwd.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?].*"))
            return "Password must contain at least one special character.";
        return null;
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void success(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}