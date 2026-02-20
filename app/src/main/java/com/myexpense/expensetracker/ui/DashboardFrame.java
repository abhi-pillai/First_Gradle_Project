package com.myexpense.expensetracker.ui;

import com.myexpense.expensetracker.model.Expense;
import com.myexpense.expensetracker.model.Income;
import com.myexpense.expensetracker.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.util.List;

public class DashboardFrame extends JFrame {

    // ── Services ────────────────────────────────────────────
    private final AuthService     authService;
    private final ExpenseService  expenseService;
    private final IncomeService   incomeService;
    private final ReportService   reportService;
    private final BudgetService   budgetService;
    private final CategoryService categoryService;

    // ── Summary labels ───────────────────────────────────────
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JLabel balanceLabel;

    // ── Expense tab ──────────────────────────────────────────
    private DefaultTableModel expenseModel;
    private JTable            expenseTable;
    private JTextField        expSearchField;
    private JComboBox<String> expCategoryBox;
    private JSpinner          expFromSpinner, expToSpinner;

    // ── Income tab ───────────────────────────────────────────
    private DefaultTableModel incomeModel;
    private JTable            incomeTable;
    private JTextField        incSearchField;
    private JSpinner          incFromSpinner, incToSpinner;

    // ── Side panel (for collapse logic) ─────────────────────
    private JPanel    sidePanel;
    private boolean   sideExpanded = true;
    private JSplitPane mainSplit;

    // ── Cached filter state ──────────────────────────────────
    private List<Expense> shownExpenses;
    private List<Income>  shownIncomes;

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
        // Start at a sensible minimum; let the user resize freely
        setMinimumSize(new Dimension(700, 500));
        setSize(1050, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildUI();
        refreshAll();

        // Re-tile summary cards when window width changes
        getContentPane().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                adaptTopBar();
            }
        });
    }

    // ════════════════════════════════════════════════════════
    //  UI BUILD
    // ════════════════════════════════════════════════════════
    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        add(buildTopBar(),  BorderLayout.NORTH);

        // JSplitPane lets the user drag the side-nav boundary
        sidePanel = buildSidePanel();
        mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidePanel, buildCenter());
        mainSplit.setDividerLocation(160);
        mainSplit.setDividerSize(4);
        mainSplit.setContinuousLayout(true);  // live repaint while dragging
        mainSplit.setBorder(null);
        // Don't let the right side get squished below a usable width
        mainSplit.setResizeWeight(0.0);       // extra space always goes to right pane

        add(mainSplit, BorderLayout.CENTER);
    }

    // ── Top bar ──────────────────────────────────────────────
    /** Outer top-bar panel; rebuilt when the window resizes. */
    private JPanel topBar;           // keep a ref for adaptTopBar()
    private JPanel summaryCards;     // the cards sub-panel

    private JPanel buildTopBar() {
        topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setBackground(new Color(30, 80, 160));
        // Use an empty border instead of a fixed preferred height so the bar
        // wraps naturally if the window is very narrow.
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // App title + user – left side, never shrinks below its preferred size
        JLabel appLabel = new JLabel("TrackIt");
        appLabel.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 28));
        appLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("Logged in as: " + authService.getLoggedInUser().getUsername());
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userLabel.setForeground(new Color(200, 220, 255));

        JPanel leftTop = new JPanel(new BorderLayout(0, 2));
        leftTop.setOpaque(false);
        leftTop.add(appLabel,  BorderLayout.NORTH);
        leftTop.add(userLabel, BorderLayout.SOUTH);

        // Collapse-nav toggle button in the far right of left block
        JButton toggleNav = new JButton("☰");
        toggleNav.setToolTipText("Toggle navigation panel");
        toggleNav.setFocusPainted(false);
        toggleNav.setBorderPainted(false);
        toggleNav.setContentAreaFilled(false);
        toggleNav.setForeground(Color.WHITE);
        toggleNav.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        toggleNav.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleNav.addActionListener(e -> toggleSidePanel());

        JPanel leftBlock = new JPanel(new BorderLayout(8, 0));
        leftBlock.setOpaque(false);
        leftBlock.add(toggleNav, BorderLayout.WEST);
        leftBlock.add(leftTop,   BorderLayout.CENTER);

        // Summary cards – right side
        summaryCards = buildSummaryCards();

        topBar.add(leftBlock,    BorderLayout.WEST);
        topBar.add(summaryCards, BorderLayout.EAST);
        return topBar;
    }

    /** Builds (or rebuilds) the three summary cards. */
    private JPanel buildSummaryCards() {
        JPanel cards = new JPanel(new GridLayout(1, 3, 10, 0));
        cards.setOpaque(false);

        // makeSummaryCard sets the instance labels as a side-effect
        JPanel incCard  = makeSummaryCardPanel("Total Income",   "₹0.00", new Color(39, 174, 96));
        JPanel expCard  = makeSummaryCardPanel("Total Expenses", "₹0.00", new Color(192, 57, 43));
        JPanel balCard  = makeSummaryCardPanel("Net Balance",    "₹0.00", new Color(41, 128, 185));

        cards.add(incCard);
        cards.add(expCard);
        cards.add(balCard);
        return cards;
    }

    /**
     * When the window gets narrow, move the summary cards below the title
     * instead of beside it (stacks vertically).
     */
    private void adaptTopBar() {
        int width = getContentPane().getWidth();
        boolean narrow = width < 820;
        // Re-layout top bar: narrow → SOUTH cards, wide → EAST cards
        BorderLayout bl = (BorderLayout) topBar.getLayout();
        Component east  = bl.getLayoutComponent(BorderLayout.EAST);
        Component south = bl.getLayoutComponent(BorderLayout.SOUTH);

        if (narrow && east != null) {
            topBar.remove(summaryCards);
            topBar.add(summaryCards, BorderLayout.SOUTH);
        } else if (!narrow && south != null) {
            topBar.remove(summaryCards);
            topBar.add(summaryCards, BorderLayout.EAST);
        }
        topBar.revalidate();
        topBar.repaint();
    }

    private JPanel makeSummaryCardPanel(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(4, 2));
        card.setBackground(accent);
        card.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        // No fixed PreferredSize – let GridLayout distribute space equally
        card.setMinimumSize(new Dimension(130, 60));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setForeground(new Color(220, 255, 220));
        titleLbl.setFont(new Font("Arial", Font.PLAIN, 11));

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Arial", Font.BOLD, 18));
        valueLbl.setForeground(Color.WHITE);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);

        // Assign instance labels
        if ("Total Income".equals(title))   incomeLabel  = valueLbl;
        if ("Total Expenses".equals(title)) expenseLabel = valueLbl;
        if ("Net Balance".equals(title))    balanceLabel = valueLbl;

        return card;
    }

    // ── Side navigation ──────────────────────────────────────
    private JPanel buildSidePanel() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(new Color(40, 44, 52));
        // No fixed preferred width – JSplitPane divider controls this
        side.setMinimumSize(new Dimension(0, 0));   // allow full collapse
        side.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));

        side.add(navButton("➕ Add Expense",  new Color(192, 57, 43),  e -> openAddExpense()));
        side.add(Box.createVerticalStrut(8));
        side.add(navButton("➕ Add Income",   new Color(39, 174, 96),  e -> openAddIncome()));
        side.add(Box.createVerticalStrut(8));
        side.add(navButton("📊 Reports",      new Color(41, 128, 185), e -> openReports()));
        side.add(Box.createVerticalStrut(8));
        side.add(navButton("💰 Budget",       new Color(142, 68, 173), e -> openBudget()));
        side.add(Box.createVerticalStrut(8));
        side.add(navButton("🗂 Categories",   new Color(22, 160, 133), e -> openCategories()));
        side.add(Box.createVerticalGlue());
        side.add(navButton("🚪 Logout",       new Color(85, 85, 85),   e -> logout()));

        return side;
    }

    private void toggleSidePanel() {
        if (sideExpanded) {
            mainSplit.setDividerLocation(0);
        } else {
            mainSplit.setDividerLocation(160);
        }
        sideExpanded = !sideExpanded;
    }

    private JButton navButton(String text, Color bg, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    // ── Center: tabbed pane ──────────────────────────────────
    private JTabbedPane buildCenter() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

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
        expenseTable = new JTable(expenseModel);
        expenseTable.setRowHeight(24);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        expenseTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        // Let the Note column absorb any extra width
        expenseTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        setColumnWidths(expenseTable, 90, 100, 90, 90, -1);

        JScrollPane scroll = new JScrollPane(expenseTable);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scroll, BorderLayout.CENTER);

        panel.add(buildExpenseActionBar(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildExpenseFilterBar() {
        // WrapLayout lets filters wrap to a second line when the window is narrow
        JPanel bar = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
        bar.setBackground(new Color(245, 247, 250));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        bar.add(new JLabel("From:"));
        expFromSpinner = makeDateSpinner();
        bar.add(expFromSpinner);

        bar.add(new JLabel("To:"));
        expToSpinner = makeDateSpinner();
        bar.add(expToSpinner);

        bar.add(new JLabel("Category:"));
        List<String> cats = categoryService.getCategoryNames(userId());
        expCategoryBox = new JComboBox<>();
        expCategoryBox.addItem("All");
        cats.forEach(expCategoryBox::addItem);
        bar.add(expCategoryBox);

        bar.add(new JLabel("Search:"));
        expSearchField = new JTextField(10);
        bar.add(expSearchField);

        JButton applyBtn = new JButton("🔍 Filter");
        applyBtn.addActionListener(e -> applyExpenseFilter());
        applyBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        bar.add(applyBtn);

        JButton clearBtn = new JButton("✕ Clear");
        clearBtn.addActionListener(e -> clearExpenseFilter());
        clearBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        bar.add(clearBtn);

        return bar;
    }

    private JPanel buildExpenseActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        bar.setBackground(new Color(245, 247, 250));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JButton editBtn   = new JButton("✏ Edit");
        JButton deleteBtn = new JButton("🗑 Delete");
        deleteBtn.setForeground(new Color(192, 57, 43));
        editBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        deleteBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        editBtn.addActionListener(e -> editSelectedExpense());
        deleteBtn.addActionListener(e -> deleteSelectedExpense());

        bar.add(editBtn);
        bar.add(deleteBtn);
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
        incomeTable = new JTable(incomeModel);
        incomeTable.setRowHeight(24);
        incomeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        incomeTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        incomeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        setColumnWidths(incomeTable, 90, 120, 90, -1);

        JScrollPane scroll = new JScrollPane(incomeTable);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scroll, BorderLayout.CENTER);

        panel.add(buildIncomeActionBar(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildIncomeFilterBar() {
        JPanel bar = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
        bar.setBackground(new Color(245, 247, 250));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        bar.add(new JLabel("From:"));
        incFromSpinner = makeDateSpinner();
        bar.add(incFromSpinner);

        bar.add(new JLabel("To:"));
        incToSpinner = makeDateSpinner();
        bar.add(incToSpinner);

        bar.add(new JLabel("Search:"));
        incSearchField = new JTextField(12);
        bar.add(incSearchField);

        JButton applyBtn = new JButton("🔍 Filter");
        applyBtn.addActionListener(e -> applyIncomeFilter());
        applyBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        bar.add(applyBtn);

        JButton clearBtn = new JButton("✕ Clear");
        clearBtn.addActionListener(e -> clearIncomeFilter());
        clearBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        bar.add(clearBtn);

        return bar;
    }

    private JPanel buildIncomeActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        bar.setBackground(new Color(245, 247, 250));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JButton editBtn   = new JButton("✏ Edit");
        JButton deleteBtn = new JButton("🗑 Delete");
        deleteBtn.setForeground(new Color(192, 57, 43));
        editBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        deleteBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        editBtn.addActionListener(e -> editSelectedIncome());
        deleteBtn.addActionListener(e -> deleteSelectedIncome());

        bar.add(editBtn);
        bar.add(deleteBtn);
        return bar;
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

        incomeLabel.setText(String.format("₹%.2f", income));
        expenseLabel.setText(String.format("₹%.2f", expense));
        balanceLabel.setText(String.format("₹%.2f", balance));

        balanceLabel.setForeground(balance >= 0 ? Color.WHITE : new Color(255, 160, 160));
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
    //  EDIT / DELETE — EXPENSES
    // ════════════════════════════════════════════════════════
    private void editSelectedExpense() {
        int row = expenseTable.getSelectedRow();
        if (row < 0) { warn("Please select an expense to edit."); return; }
        Expense exp = shownExpenses.get(row);
        new EditExpenseDialog(this, exp, expenseService, categoryService, userId(), this::refreshAll)
                .setVisible(true);
    }

    private void deleteSelectedExpense() {
        int row = expenseTable.getSelectedRow();
        if (row < 0) { warn("Please select an expense to delete."); return; }
        Expense exp = shownExpenses.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete expense of ₹" + String.format("%.2f", exp.getAmount()) + " on " + exp.getDate() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                expenseService.deleteExpense(userId(), exp.getId());
                refreshAll();
            } catch (Exception ex) {
                error("Failed to delete: " + ex.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════
    //  EDIT / DELETE — INCOME
    // ════════════════════════════════════════════════════════
    private void editSelectedIncome() {
        int row = incomeTable.getSelectedRow();
        if (row < 0) { warn("Please select an income entry to edit."); return; }
        Income inc = shownIncomes.get(row);
        new EditIncomeDialog(this, inc, incomeService, userId(), this::refreshAll)
                .setVisible(true);
    }

    private void deleteSelectedIncome() {
        int row = incomeTable.getSelectedRow();
        if (row < 0) { warn("Please select an income entry to delete."); return; }
        Income inc = shownIncomes.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete income of ₹" + String.format("%.2f", inc.getAmount()) + " from " + inc.getSource() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                incomeService.deleteIncome(userId(), inc.getId());
                refreshAll();
            } catch (Exception ex) {
                error("Failed to delete: " + ex.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════
    //  NAV BUTTON ACTIONS
    // ════════════════════════════════════════════════════════
    private void openAddExpense() {
        new AddExpenseDialog(this, expenseService, categoryService, userId(), this::refreshAll)
                .setVisible(true);
    }

    private void openAddIncome() {
        new AddIncomeDialog(this, incomeService, userId(), this::refreshAll)
                .setVisible(true);
    }

    private void openReports() {
        new ReportsDialog(this, reportService, expenseService, incomeService, userId())
                .setVisible(true);
    }

    private void openBudget() {
        new BudgetDialog(this, budgetService, categoryService, userId())
                .setVisible(true);
        refreshSummary();
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
    private String userId() {
        return authService.getLoggedInUser().getId();
    }

    /** Set preferred widths on the first N columns; pass -1 to skip (last col absorbs rest). */
    private void setColumnWidths(JTable table, int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            if (widths[i] < 0) continue;
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
            col.setMinWidth(40);
        }
    }

    private JSpinner makeDateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(105, 26));
        spinner.setValue(java.util.Date.from(
                LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        return spinner;
    }

    private void resetSpinner(JSpinner spinner) {
        spinner.setValue(java.util.Date.from(
                LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
    }

    private LocalDate spinnerToDate(JSpinner spinner) {
        java.util.Date d = (java.util.Date) spinner.getValue();
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private void warn(String msg)  { JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE); }
    private void error(String msg) { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE); }

    // ════════════════════════════════════════════════════════
    //  WrapLayout – inner static class
    //  (A FlowLayout that wraps rows instead of clipping them.
    //   Based on the well-known public-domain WrapLayout by Rob Camick.)
    // ════════════════════════════════════════════════════════
    private static class WrapLayout extends FlowLayout {

        WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0, rowHeight = 0;

                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        if (rowWidth != 0) rowWidth += hgap;
                        rowWidth  += d.width;
                        rowHeight  = Math.max(rowHeight, d.height);
                    }
                }
                addRow(dim, rowWidth, rowHeight);

                dim.width  += insets.left + insets.right + hgap * 2;
                dim.height += insets.top + insets.bottom + vgap * 2;
                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width   = Math.max(dim.width, rowWidth);
            if (dim.height > 0) dim.height += getVgap();
            dim.height += rowHeight;
        }
    }
}