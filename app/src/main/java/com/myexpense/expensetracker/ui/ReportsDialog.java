package com.myexpense.expensetracker.ui;

import com.myexpense.expensetracker.service.ExpenseService;
import com.myexpense.expensetracker.service.IncomeService;
import com.myexpense.expensetracker.service.ReportService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ReportsDialog extends JDialog {

    private final ReportService  reportService;
    private final ExpenseService expenseService;
    private final IncomeService  incomeService;
    private final String         userId;

    private JTextArea summaryArea;
    private ChartPanel chartPanel;
    private JComboBox<String> monthBox;

    public ReportsDialog(JFrame parent, ReportService reportService,
                         ExpenseService expenseService, IncomeService incomeService,
                         String userId) {
        super(parent, "Reports", true);
        this.reportService  = reportService;
        this.expenseService = expenseService;
        this.incomeService  = incomeService;
        this.userId         = userId;

        setSize(660, 580);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(8, 8));

        add(buildTopControls(), BorderLayout.NORTH);
        add(buildMainArea(),    BorderLayout.CENTER);
        add(buildExportBar(),   BorderLayout.SOUTH);

        refreshReport(YearMonth.now());
    }

    // ── Top: month selector ──────────────────────────────────
    private JPanel buildTopControls() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        // Last 12 months in the dropdown
        monthBox = new JComboBox<>();
        YearMonth current = YearMonth.now();
        for (int i = 0; i < 12; i++) {
            monthBox.addItem(current.minusMonths(i).format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        }
        monthBox.setSelectedIndex(0);

        JButton viewBtn = new JButton("View Report");
        viewBtn.addActionListener(e -> {
            int idx = monthBox.getSelectedIndex();
            refreshReport(YearMonth.now().minusMonths(idx));
        });

        bar.add(new JLabel("Month:"));
        bar.add(monthBox);
        bar.add(viewBtn);
        return bar;
    }

    // ── Main: summary text + bar chart ───────────────────────
    private JSplitPane buildMainArea() {
        summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        summaryArea.setMargin(new Insets(10, 14, 10, 14));

        chartPanel = new ChartPanel();
        chartPanel.setPreferredSize(new Dimension(0, 220));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(summaryArea), chartPanel);
        split.setDividerLocation(200);
        split.setResizeWeight(0.4);
        return split;
    }

    // ── Bottom: export buttons ───────────────────────────────
    private JPanel buildExportBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JButton expPdfBtn  = new JButton("📄 Expenses PDF");
        JButton incPdfBtn  = new JButton("📄 Income PDF");
        expPdfBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        incPdfBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        expPdfBtn.addActionListener(e -> exportExpensePdf());
        incPdfBtn.addActionListener(e -> exportIncomePdf());

        bar.add(expPdfBtn);
        bar.add(new JSeparator(SwingConstants.VERTICAL));
        bar.add(incPdfBtn);
        return bar;
    }

    // ── Data refresh ─────────────────────────────────────────
    private void refreshReport(YearMonth month) {
        Map<String, Double> monthly   = reportService.getMonthlySummary(userId, month);
        Map<String, Double> breakdown = reportService.getCategoryBreakdownForMonth(userId, month);
        double allTimeIncome   = reportService.getTotalIncome(userId);
        double allTimeExpenses = reportService.getTotalExpenses(userId);
        double allTimeSavings  = reportService.getNetSavings(userId);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("══ %s ══\n\n",
                month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))));
        sb.append(String.format("  Income  :  ₹%,.2f\n", monthly.get("Income")));
        sb.append(String.format("  Expenses:  ₹%,.2f\n", monthly.get("Expense")));
        sb.append(String.format("  Savings :  ₹%,.2f\n\n", monthly.get("Savings")));

        sb.append("── Category Breakdown ──\n");
        if (breakdown.isEmpty()) {
            sb.append("  No expenses recorded.\n");
        } else {
            breakdown.forEach((cat, amt) ->
                    sb.append(String.format("  %-18s ₹%,.2f\n", cat, amt)));
        }

        sb.append("\n── All-Time Summary ──\n");
        sb.append(String.format("  Total Income  :  ₹%,.2f\n", allTimeIncome));
        sb.append(String.format("  Total Expenses:  ₹%,.2f\n", allTimeExpenses));
        sb.append(String.format("  Net Savings   :  ₹%,.2f\n", allTimeSavings));

        summaryArea.setText(sb.toString());
        chartPanel.setData(breakdown);
        chartPanel.repaint();
    }

    // ── Exports ──────────────────────────────────────────────
   
    private void exportExpensePdf() {
        String downloadPath = System.getProperty("user.home") + "/Downloads";
        String path = chooseSavePath(downloadPath+"/expenses_report.pdf", "pdf");
        if (path == null) return;
        try { expenseService.exportToPdf(userId, path);
              success("Expenses exported to PDF."); }
        catch (Exception ex) { error(ex); }
    }

    
    private void exportIncomePdf() {
        String downloadPath = System.getProperty("user.home") + "/Downloads";
        String path = chooseSavePath(downloadPath+"/income_report.pdf", "pdf");
        if (path == null) return;
        try { incomeService.exportToPdf(userId, path);
              success("Income exported to PDF."); }
        catch (Exception ex) { error(ex); }
    }

    private String chooseSavePath(String defaultName, String ext) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File(defaultName));
        fc.setFileFilter(new FileNameExtensionFilter(ext.toUpperCase() + " files", ext));
        int res = fc.showSaveDialog(this);
        return res == JFileChooser.APPROVE_OPTION ? fc.getSelectedFile().getAbsolutePath() : null;
    }

    private void success(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Done", JOptionPane.INFORMATION_MESSAGE);
    }
    private void error(Exception ex) {
        JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ════════════════════════════════════════════════════════
    //  Inner class: simple bar chart
    // ════════════════════════════════════════════════════════
     static class ChartPanel extends JPanel {

        private Map<String, Double> data;

        void setData(Map<String, Double> data) { this.data = data; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.WHITE);

            if (data == null || data.isEmpty()) {
                g.setColor(Color.GRAY);
                g.setFont(new Font("Arial", Font.ITALIC, 13));
                g.drawString("No data for selected month.", 20, getHeight() / 2);
                return;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color[] palette = {
                new Color(52, 152, 219), new Color(46, 204, 113),
                new Color(231, 76, 60),  new Color(155, 89, 182),
                new Color(230, 126, 34), new Color(26, 188, 156),
                new Color(241, 196, 15), new Color(52, 73, 94),
                new Color(236, 112, 99), new Color(93, 173, 226)
            };

            String[] keys  = data.keySet().toArray(new String[0]);
            double   total = data.values().stream().mapToDouble(Double::doubleValue).sum();

            // ── Layout: pie on left, legend on right ──
            int size       = Math.min(getWidth() / 2, getHeight()) - 40;
            int pieX       = 20;
            int pieY       = (getHeight() - size) / 2;
            int legendX    = pieX + size + 30;
            int legendY    = pieY + 10;

            // ── Draw pie slices ──
            double startAngle = 0;
            for (int i = 0; i < keys.length; i++) {
                double pct   = data.get(keys[i]) / total;
                double sweep = pct * 360.0;

                // Fill slice
                g2.setColor(palette[i % palette.length]);
                g2.fillArc(pieX, pieY, size, size, (int) startAngle, (int) Math.ceil(sweep));

                // Slice border
                g2.setColor(Color.WHITE);
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawArc(pieX, pieY, size, size, (int) startAngle, (int) Math.ceil(sweep));

                // Percentage label inside slice (only if slice is big enough)
                if (pct > 0.05) {
                    double midAngle = Math.toRadians(startAngle + sweep / 2);
                    int lx = (int) (pieX + size / 2 + (size / 2) * 0.6 * Math.cos(midAngle));
                    int ly = (int) (pieY + size / 2 - (size / 2) * 0.6 * Math.sin(midAngle));
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Arial", Font.BOLD, 11));
                    String pctStr = String.format("%.0f%%", pct * 100);
                    int sw = g2.getFontMetrics().stringWidth(pctStr);
                    g2.drawString(pctStr, lx - sw / 2, ly + 4);
                }

                startAngle += sweep;
            }

            // ── Outer circle border ──
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new java.awt.BasicStroke(1.5f));
            g2.drawOval(pieX, pieY, size, size);

            // ── Legend ──
            int boxSize    = 14;
            int lineHeight = 22;
            g2.setFont(new Font("Arial", Font.PLAIN, 12));

            for (int i = 0; i < keys.length; i++) {
                int ly = legendY + i * lineHeight;

                // Color box
                g2.setColor(palette[i % palette.length]);
                g2.fillRoundRect(legendX, ly, boxSize, boxSize, 4, 4);
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawRoundRect(legendX, ly, boxSize, boxSize, 4, 4);

                // Label: "Category  ₹amount (xx%)"
                double pct = data.get(keys[i]) / total * 100;
                String label = String.format("%s  ₹%.0f  (%.1f%%)",
                        keys[i], data.get(keys[i]), pct);
                g2.setColor(new Color(50, 50, 50));
                g2.drawString(label, legendX + boxSize + 8, ly + boxSize - 1);
            }

            // ── Total label below pie ──
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.setColor(new Color(80, 80, 80));
            String totalStr = String.format("Total: ₹%.2f", total);
            int tw = g2.getFontMetrics().stringWidth(totalStr);
            g2.drawString(totalStr, pieX + size / 2 - tw / 2, pieY + size + 20);

        }
    }
}