package com.myexpense.expensetracker.ui;

import com.myexpense.expensetracker.model.Expense;
import com.myexpense.expensetracker.model.Income;
import com.myexpense.expensetracker.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;

public class DashboardFrame extends JFrame {

    // ── Services ─────────────────────────────────────────────
    private final AuthService     authService;
    private final ExpenseService  expenseService;
    private final IncomeService   incomeService;
    private final ReportService   reportService;
    private final BudgetService   budgetService;
    private final CategoryService categoryService;

    // ── Summary labels ───────────────────────────────────────
    private JLabel incomeValueLabel;
    private JLabel expenseValueLabel;
    private JLabel balanceValueLabel;

    // ── Expense tab ──────────────────────────────────────────
    private DefaultTableModel expenseModel;
    private JTable            expenseTable;
    private JTextField        expSearchField;
    private JComboBox<String> expCategoryBox;
    private JSpinner          expFromSpinner, expToSpinner;
    private List<Expense>     shownExpenses;

    // ── Income tab ───────────────────────────────────────────
    private DefaultTableModel incomeModel;
    private JTable            incomeTable;
    private JTextField        incSearchField;
    private JSpinner          incFromSpinner, incToSpinner;
    private List<Income>      shownIncomes;

    // ── Design constants ─────────────────────────────────────
    private static final Color NAV_BG        = new Color(28, 32, 42);
    private static final Color NAV_HOVER     = new Color(40, 46, 60);
    private static final Color FILTER_BG     = new Color(248, 249, 252);
    private static final Color TABLE_EVEN    = new Color(247, 249, 252);
    private static final Color TABLE_SELECT  = new Color(210, 228, 255);
    private static final Color ACCENT_RED    = new Color(192, 57,  43);
    private static final Color ACCENT_GREEN  = new Color(39,  174, 96);
    private static final Color ACCENT_BLUE   = new Color(41,  128, 185);
    private static final Color ACCENT_PURPLE = new Color(142, 68,  173);
    private static final Color ACCENT_TEAL   = new Color(22,  160, 133);
    private static final Color ACCENT_GRAY   = new Color(80,  85,  95);

    // ────────────────────────────────────────────────────────
    public DashboardFrame(AuthService authService,
                          ExpenseService expenseService,
                          IncomeService incomeService,
                          ReportService reportService,
                          BudgetService budgetService,
                          CategoryService categoryService) {
        this.authService     = authService;
        this.expenseService  = expenseService;
        this.incomeService   = incomeService;
        this.reportService   = reportService;
        this.budgetService   = budgetService;
        this.categoryService = categoryService;

        setTitle("TrackIt – Dashboard");
        setMinimumSize(new Dimension(900, 580));
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildUI();
        refreshAll();
    }

    // ════════════════════════════════════════════════════════
    //  UI BUILD
    // ════════════════════════════════════════════════════════
    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildSidePanel(), BorderLayout.WEST);
        add(buildCenter(),    BorderLayout.CENTER);
    }

    // ── Top bar ──────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(18, 52, 120),
                        getWidth(), 0, new Color(35, 90, 170));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bar.setPreferredSize(new Dimension(0, 100));
        bar.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Left: app name + user
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);

        JLabel appLabel = new JLabel("TrackIt");
        appLabel.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 30));
        appLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("  Welcome, " + authService.getLoggedInUser().getUsername());
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userLabel.setForeground(new Color(180, 210, 255));

        left.add(appLabel);
        left.add(userLabel);

        // Right: summary cards
        JPanel cards = new JPanel(new GridLayout(1, 3, 10, 0));
        cards.setOpaque(false);

        JPanel incCard  = buildSummaryCard("Total Income",   ACCENT_GREEN);
        JPanel expCard  = buildSummaryCard("Total Expenses", ACCENT_RED);
        JPanel balCard  = buildSummaryCard("Net Balance",    ACCENT_BLUE);

        // Store value labels for later update
        incomeValueLabel  = (JLabel) incCard.getComponent(1);
        expenseValueLabel = (JLabel) expCard.getComponent(1);
        balanceValueLabel = (JLabel) balCard.getComponent(1);

        cards.add(incCard);
        cards.add(expCard);
        cards.add(balCard);

        bar.add(left,  BorderLayout.WEST);
        bar.add(cards, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildSummaryCard(String title, Color accent) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(8, 14, 8, 14));
        card.setPreferredSize(new Dimension(180, 76));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        titleLbl.setForeground(new Color(200, 220, 255));

        JLabel valueLbl = new JLabel("₹0.00");
        valueLbl.setFont(new Font("Arial", Font.BOLD, 20));
        valueLbl.setForeground(Color.WHITE);

        card.add(titleLbl);
        card.add(valueLbl);
        return card;
    }

    // ── Side navigation ──────────────────────────────────────
    private JPanel buildSidePanel() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(NAV_BG);
        side.setPreferredSize(new Dimension(170, 0));
        side.setBorder(new EmptyBorder(16, 8, 16, 8));

        side.add(navButton("➕ Add Expense",  ACCENT_RED,    e -> openAddExpense()));
        side.add(Box.createVerticalStrut(6));
        side.add(navButton("➕ Add Income",   ACCENT_GREEN,  e -> openAddIncome()));
        side.add(Box.createVerticalStrut(6));
        side.add(navButton("📊 Reports",      ACCENT_BLUE,   e -> openReports()));
        side.add(Box.createVerticalStrut(6));
        side.add(navButton("💰 Budget",       ACCENT_PURPLE, e -> openBudget()));
        side.add(Box.createVerticalStrut(6));
        side.add(navButton("🗂 Categories",   ACCENT_TEAL,   e -> openCategories()));
        side.add(Box.createVerticalStrut(6));
        side.add(navButton("⚙ Account",       new Color(52, 73, 94), e -> openAccountSettings()));
        side.add(Box.createVerticalGlue());
        side.add(navButton("🚪 Logout",       ACCENT_GRAY,   e -> logout()));

        return side;
    }

    private JButton navButton(String text, Color accent, ActionListener al) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? accent.darker()
                         : hovered               ? accent
                         : NAV_HOVER;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Left accent strip when not hovered
                if (!hovered && !getModel().isPressed()) {
                    g2.setColor(accent);
                    g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                }
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(8, 12, 8, 8));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    // ── Center: tabbed pane ──────────────────────────────────
    private JTabbedPane buildCenter() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        tabs.setBorder(new EmptyBorder(4, 4, 4, 4));
        tabs.addTab("💸 Expenses", buildExpenseTab());
        tabs.addTab("💰 Income",   buildIncomeTab());
        return tabs;
    }

    // ── Expense tab ──────────────────────────────────────────
    private JPanel buildExpenseTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.add(buildExpenseFilterBar(), BorderLayout.NORTH);

        String[] cols = {"Date", "Category", "Amount (₹)", "Payment", "Note"};
        expenseModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        expenseTable = buildStyledTable(expenseModel);
        panel.add(styledScrollPane(expenseTable), BorderLayout.CENTER);
        panel.add(buildActionBar(
                "✏  Edit",   e -> editSelectedExpense(),
                "🗑  Delete", e -> deleteSelectedExpense()
        ), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildExpenseFilterBar() {
        JPanel bar = buildFilterBar();

        bar.add(filterLabel("From:"));
        expFromSpinner = makeDateSpinner();
        bar.add(expFromSpinner);

        bar.add(filterLabel("To:"));
        expToSpinner = makeDateSpinner();
        bar.add(expToSpinner);

        bar.add(filterLabel("Category:"));
        expCategoryBox = new JComboBox<>();
        expCategoryBox.addItem("All");
        categoryService.getCategoryNames(userId()).forEach(expCategoryBox::addItem);
        expCategoryBox.setPreferredSize(new Dimension(130, 28));
        bar.add(expCategoryBox);

        bar.add(filterLabel("Search:"));
        expSearchField = styledSearchField(12);
        bar.add(expSearchField);

        bar.add(filterButton("🔍  Filter", ACCENT_BLUE,  e -> applyExpenseFilter()));
        bar.add(filterButton("✕  Clear",   ACCENT_GRAY,  e -> clearExpenseFilter()));
        return bar;
    }

    // ── Income tab ───────────────────────────────────────────
    private JPanel buildIncomeTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.add(buildIncomeFilterBar(), BorderLayout.NORTH);

        String[] cols = {"Date", "Source", "Amount (₹)", "Note"};
        incomeModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        incomeTable = buildStyledTable(incomeModel);
        panel.add(styledScrollPane(incomeTable), BorderLayout.CENTER);
        panel.add(buildActionBar(
                "✏  Edit",   e -> editSelectedIncome(),
                "🗑  Delete", e -> deleteSelectedIncome()
        ), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildIncomeFilterBar() {
        JPanel bar = buildFilterBar();

        bar.add(filterLabel("From:"));
        incFromSpinner = makeDateSpinner();
        bar.add(incFromSpinner);

        bar.add(filterLabel("To:"));
        incToSpinner = makeDateSpinner();
        bar.add(incToSpinner);

        bar.add(filterLabel("Search:"));
        incSearchField = styledSearchField(16);
        bar.add(incSearchField);

        bar.add(filterButton("🔍  Filter", ACCENT_BLUE, e -> applyIncomeFilter()));
        bar.add(filterButton("✕  Clear",   ACCENT_GRAY, e -> clearIncomeFilter()));
        return bar;
    }

    // ════════════════════════════════════════════════════════
    //  STYLED COMPONENT HELPERS
    // ════════════════════════════════════════════════════════
    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(TABLE_SELECT);
                    c.setForeground(new Color(20, 20, 60));
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_EVEN);
                    c.setForeground(new Color(40, 40, 60));
                }
                if (c instanceof JComponent jc) {
                    jc.setBorder(new EmptyBorder(0, 8, 0, 8));
                }
                return c;
            }
        };
        table.setRowHeight(30);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 233, 240));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(235, 238, 248));
        header.setForeground(new Color(50, 60, 100));
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(180, 195, 230)));

        return table;
    }

    private JScrollPane styledScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(220, 225, 235)));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bar.setBackground(FILTER_BG);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 235)));
        return bar;
    }

    private JPanel buildActionBar(String lbl1, ActionListener al1,
                                   String lbl2, ActionListener al2) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bar.setBackground(FILTER_BG);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 235)));
        bar.add(filterButton(lbl1, ACCENT_BLUE,  al1));
        bar.add(filterButton(lbl2, ACCENT_RED,   al2));
        return bar;
    }

    private JLabel filterLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl.setForeground(new Color(80, 90, 110));
        return lbl;
    }

    private JTextField styledSearchField(int cols) {
        JTextField field = new JTextField(cols) {
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? ACCENT_BLUE : new Color(200, 210, 230));
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
            }
        };
        field.setBorder(new EmptyBorder(4, 8, 4, 8));
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 28));
        return field;
    }

    private JButton filterButton(String text, Color accent, ActionListener al) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? accent.darker()
                        : getModel().isRollover() ? accent.brighter() : accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(4, 12, 4, 12));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 16, 28));
        btn.addActionListener(al);
        return btn;
    }

    // ════════════════════════════════════════════════════════
    //  DATA REFRESH
    // ════════════════════════════════════════════════════════
    public void refreshAll() {
        refreshSummary();
        loadExpenses(expenseService.getAllExpenses(userId()));
        loadIncomes(incomeService.getAllIncome(userId()));
    }

    private void refreshSummary() {
        double income  = reportService.getTotalIncome(userId());
        double expense = reportService.getTotalExpenses(userId());
        double balance = income - expense;

        incomeValueLabel.setText(String.format("₹%.2f", income));
        expenseValueLabel.setText(String.format("₹%.2f", expense));
        balanceValueLabel.setText(String.format("₹%.2f", balance));
        balanceValueLabel.setForeground(balance >= 0 ? Color.WHITE : new Color(255, 160, 160));
    }

    private void loadExpenses(List<Expense> expenses) {
        shownExpenses = expenses;
        expenseModel.setRowCount(0);
        for (Expense e : expenses) {
            expenseModel.addRow(new Object[]{
                    e.getDate(), e.getCategory(),
                    String.format("%.2f", e.getAmount()),
                    e.getPaymentMethod(), e.getNote()
            });
        }
    }

    private void loadIncomes(List<Income> incomes) {
        shownIncomes = incomes;
        incomeModel.setRowCount(0);
        for (Income i : incomes) {
            incomeModel.addRow(new Object[]{
                    i.getDate(), i.getSource(),
                    String.format("%.2f", i.getAmount()),
                    i.getNote()
            });
        }
    }

    // ════════════════════════════════════════════════════════
    //  FILTER ACTIONS
    // ════════════════════════════════════════════════════════
    private void applyExpenseFilter() {
        LocalDate from  = spinnerToDate(expFromSpinner);
        LocalDate to    = spinnerToDate(expToSpinner);
        String category = (String) expCategoryBox.getSelectedItem();
        String keyword  = expSearchField.getText().trim();
        if ("All".equals(category)) category = null;
        loadExpenses(expenseService.filterAdvanced(userId(), from, to, category, keyword));
    }

    private void clearExpenseFilter() {
        expSearchField.setText("");
        expCategoryBox.setSelectedIndex(0);
        resetSpinner(expFromSpinner);
        resetSpinner(expToSpinner);
        loadExpenses(expenseService.getAllExpenses(userId()));
    }

    private void applyIncomeFilter() {
        LocalDate from = spinnerToDate(incFromSpinner);
        LocalDate to   = spinnerToDate(incToSpinner);
        String keyword = incSearchField.getText().trim();
        loadIncomes(incomeService.filterAdvanced(userId(), from, to, null, keyword));
    }

    private void clearIncomeFilter() {
        incSearchField.setText("");
        resetSpinner(incFromSpinner);
        resetSpinner(incToSpinner);
        loadIncomes(incomeService.getAllIncome(userId()));
    }

    // ════════════════════════════════════════════════════════
    //  EDIT / DELETE
    // ════════════════════════════════════════════════════════
    private void editSelectedExpense() {
        int row = expenseTable.getSelectedRow();
        if (row < 0) { warn("Please select an expense to edit."); return; }
        new EditExpenseDialog(this, shownExpenses.get(row), expenseService,
                categoryService, userId(), this::refreshAll).setVisible(true);
    }

    private void deleteSelectedExpense() {
        int row = expenseTable.getSelectedRow();
        if (row < 0) { warn("Please select an expense to delete."); return; }
        Expense exp = shownExpenses.get(row);
        if (confirm("Delete expense of ₹" + String.format("%.2f", exp.getAmount())
                + " on " + exp.getDate() + "?")) {
            try { expenseService.deleteExpense(userId(), exp.getId()); refreshAll(); }
            catch (Exception ex) { error("Failed to delete: " + ex.getMessage()); }
        }
    }

    private void editSelectedIncome() {
        int row = incomeTable.getSelectedRow();
        if (row < 0) { warn("Please select an income entry to edit."); return; }
        new EditIncomeDialog(this, shownIncomes.get(row), incomeService,
                userId(), this::refreshAll).setVisible(true);
    }

    private void deleteSelectedIncome() {
        int row = incomeTable.getSelectedRow();
        if (row < 0) { warn("Please select an income entry to delete."); return; }
        Income inc = shownIncomes.get(row);
        if (confirm("Delete income of ₹" + String.format("%.2f", inc.getAmount())
                + " from " + inc.getSource() + "?")) {
            try { incomeService.deleteIncome(userId(), inc.getId()); refreshAll(); }
            catch (Exception ex) { error("Failed to delete: " + ex.getMessage()); }
        }
    }

    // ════════════════════════════════════════════════════════
    //  NAV ACTIONS
    // ════════════════════════════════════════════════════════
    private void openAddExpense() {
        new AddExpenseDialog(this, expenseService, categoryService,
                userId(), this::refreshAll).setVisible(true);
    }

    private void openAddIncome() {
        new AddIncomeDialog(this, incomeService, userId(), this::refreshAll).setVisible(true);
    }

    private void openReports() {
        new ReportsDialog(this, reportService, expenseService, incomeService, userId()).setVisible(true);
    }

    private void openBudget() {
        new BudgetDialog(this, budgetService, categoryService, userId()).setVisible(true);
        refreshSummary();
    }

    private void openAccountSettings() {
        new AccountSettingsDialog(this, authService, () -> {
            // Account deleted — log out and return to login screen
            new LoginFrame(authService, expenseService, incomeService,
                    reportService, budgetService, categoryService).setVisible(true);
            dispose();
        }).setVisible(true);
    }

    private void openCategories() {
        new ManageCategoriesDialog(this, categoryService, userId(), this::refreshCategoryDropdown)
                .setVisible(true);
    }

    private void refreshCategoryDropdown() {
        expCategoryBox.removeAllItems();
        expCategoryBox.addItem("All");
        categoryService.getCategoryNames(userId()).forEach(expCategoryBox::addItem);
        refreshAll();
    }

    private void logout() {
        authService.logout();
        new LoginFrame(authService, expenseService, incomeService,
                reportService, budgetService, categoryService).setVisible(true);
        dispose();
    }

    // ════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════
    private String userId() { return authService.getLoggedInUser().getId(); }

    private JSpinner makeDateSpinner() {
        JSpinner s = new JSpinner(new SpinnerDateModel());
        s.setEditor(new JSpinner.DateEditor(s, "yyyy-MM-dd"));
        s.setPreferredSize(new Dimension(105, 28));
        s.setValue(java.util.Date.from(
                LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        return s;
    }

    private void resetSpinner(JSpinner s) {
        s.setValue(java.util.Date.from(
                LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
    }

    private LocalDate spinnerToDate(JSpinner s) {
        return ((java.util.Date) s.getValue()).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void warn(String msg)  { JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE); }
    private void error(String msg) { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE); }
}