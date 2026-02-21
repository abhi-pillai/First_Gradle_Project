package com.myexpense.expensetracker.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.myexpense.expensetracker.model.Expense;
import com.myexpense.expensetracker.repository.ExpenseRepository;

import java.awt.Color;
import java.io.FileOutputStream;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

public class ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    // ===============================
    // CRUD  (all scoped to userId)
    // ===============================

    public void addExpense(Expense expense) throws Exception {
        repository.save(expense);
    }

    public List<Expense> getAllExpenses(String userId) {
        return repository.loadByUser(userId);
    }

    public void deleteExpense(String userId, String id) throws Exception {
        List<Expense> updated = repository.loadByUser(userId).stream()
                .filter(e -> !e.getId().equals(id))
                .collect(Collectors.toList());
        repository.overwriteForUser(userId, updated);
    }

    public void updateExpense(String userId, Expense updatedExpense) throws Exception {
        List<Expense> updated = repository.loadByUser(userId).stream()
                .map(e -> e.getId().equals(updatedExpense.getId()) ? updatedExpense : e)
                .collect(Collectors.toList());
        repository.overwriteForUser(userId, updated);
    }

    // ===============================
    // TOTAL CALCULATIONS
    // ===============================

    public double getTotalExpenses(String userId) {
        return repository.loadByUser(userId).stream().mapToDouble(Expense::getAmount).sum();
    }

    public double getTotalByCategory(String userId, String category) {
        return repository.loadByUser(userId).stream()
                .filter(e -> e.getCategory().equalsIgnoreCase(category))
                .mapToDouble(Expense::getAmount).sum();
    }

    public double getTotalByDateRange(String userId, LocalDate start, LocalDate end) {
        return repository.loadByUser(userId).stream()
                .filter(e -> !e.getDate().isBefore(start) && !e.getDate().isAfter(end))
                .mapToDouble(Expense::getAmount).sum();
    }

    public double getMonthlyTotal(String userId, YearMonth month) {
        return repository.loadByUser(userId).stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(month))
                .mapToDouble(Expense::getAmount).sum();
    }

    public double getDailyTotal(String userId, LocalDate date) {
        return repository.loadByUser(userId).stream()
                .filter(e -> e.getDate().equals(date))
                .mapToDouble(Expense::getAmount).sum();
    }

    // ===============================
    // FILTERING
    // ===============================

    public List<Expense> filterByDateRange(String userId, LocalDate start, LocalDate end) {
        return repository.loadByUser(userId).stream()
                .filter(e -> !e.getDate().isBefore(start) && !e.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    public List<Expense> filterByCategory(String userId, String category) {
        return repository.loadByUser(userId).stream()
                .filter(e -> e.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<Expense> searchByKeyword(String userId, String keyword) {
        String kw = keyword.toLowerCase();
        return repository.loadByUser(userId).stream()
                .filter(e -> (e.getNote() != null && e.getNote().toLowerCase().contains(kw))
                        || e.getCategory().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    public List<Expense> filterAdvanced(String userId, LocalDate start, LocalDate end,
                                        String category, String keyword) {
        return repository.loadByUser(userId).stream()
                .filter(e -> start == null || !e.getDate().isBefore(start))
                .filter(e -> end   == null || !e.getDate().isAfter(end))
                .filter(e -> category == null || category.isEmpty()
                        || e.getCategory().equalsIgnoreCase(category))
                .filter(e -> keyword == null || keyword.isEmpty()
                        || (e.getNote() != null && e.getNote().toLowerCase().contains(keyword.toLowerCase()))
                        || e.getCategory().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    // ===============================
    // EXPORT PDF
    // ===============================

    public void exportToPdf(String userId, String filePath) throws Exception {
        List<Expense> expenses = repository.loadByUser(userId);

        java.io.InputStream fontStream = ExpenseService.class
                .getResourceAsStream("/fonts/NotoSans_ExtraCondensed-Regular.ttf");
        byte[] fontBytes = fontStream.readAllBytes();
        BaseFont baseFont = BaseFont.createFont(
                "NotoSans.ttf",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,
                true,
                fontBytes,
                null
        );

        Document document = new Document(PageSize.A4, 40, 40, 60, 60);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

        // ── Fonts ────────────────────────────────────────────────
        Font appTitleFont = new Font(baseFont, 36, Font.BOLDITALIC, new Color(46, 90, 136));
        Font tagFont      = new Font(baseFont, 10, Font.ITALIC,     new Color(108, 117, 125));
        Font titleFont    = new Font(Font.HELVETICA, 22, Font.BOLD, new Color(33, 37, 41));
        Font subFont      = new Font(Font.TIMES_ROMAN, 10, Font.NORMAL, new Color(108, 117, 125));
        Font headerFont   = new Font(baseFont, 10, Font.BOLD,   Color.WHITE);
        Font cellFont     = new Font(baseFont, 10, Font.NORMAL, new Color(33, 37, 41));
        Font totalFont    = new Font(baseFont, 11, Font.BOLD,   new Color(33, 37, 41));

        // ── Colors ───────────────────────────────────────────────
        Color headerColor = new Color(46, 90, 136);
        Color accentColor = new Color(52, 152, 219);
        Color rowEven     = new Color(245, 247, 250);
        Color rowOdd      = Color.WHITE;

        // ── Page border on every page ────────────────────────────
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
                PdfContentByte cb = w.getDirectContent();
                cb.setColorStroke(new Color(46, 90, 136));
                cb.setLineWidth(2f);
                cb.rectangle(
                        20, 20,
                        d.getPageSize().getWidth()  - 40,
                        d.getPageSize().getHeight() - 40
                );
                cb.stroke();
            }
        });

        document.open();

        // ── App title ────────────────────────────────────────────
        Paragraph appTitle = new Paragraph("TrackIt", appTitleFont);
        appTitle.setAlignment(Element.ALIGN_CENTER);
        appTitle.setSpacingAfter(4);
        document.add(appTitle);

        Paragraph tagline = new Paragraph("Track your money, own your day.", tagFont);
        tagline.setAlignment(Element.ALIGN_CENTER);
        tagline.setSpacingAfter(12);
        document.add(tagline);

        // ── Report title ─────────────────────────────────────────
        Paragraph title = new Paragraph("Expense Report", titleFont);
        title.setAlignment(Element.ALIGN_LEFT);
        title.setSpacingAfter(4);
        document.add(title);

        Paragraph sub = new Paragraph("Generated on: " + LocalDate.now(), subFont);
        sub.setAlignment(Element.ALIGN_LEFT);
        sub.setSpacingAfter(14);
        document.add(sub);

        // ── Expense table (4 columns) ────────────────────────────
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 2.5f, 1.8f, 2.5f});
        table.setSpacingBefore(0);
        table.setSpacingAfter(0);  // no gap between table and total row

        // Header row
        for (String h : new String[]{"Date", "Category", "Payment", "Amount"}) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(headerColor);
            cell.setPadding(8);
            cell.setBorder(Rectangle.BOX);
            cell.setBorderWidth(1.0f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }

        // Data rows
        boolean even = false;
        for (Expense e : expenses) {
            Color rowColor = even ? rowEven : rowOdd;
            even = !even;

            String[] values = {
                e.getDate().toString(),
                e.getCategory(),
                e.getPaymentMethod().toString(),
                String.format("%.2f", e.getAmount()),
            };

            for (int i = 0; i < values.length; i++) {
                PdfPCell cell = new PdfPCell(new Phrase(values[i], cellFont));
                cell.setBackgroundColor(rowColor);
                cell.setPadding(7);
                cell.setBorder(Rectangle.BOX);
                cell.setBorderWidth(1.0f);
                // Amount column right-aligned, rest left-aligned
                cell.setHorizontalAlignment(i == 2 ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }
        }

        document.add(table);

        // ── Total row — same 4 columns, same widths ───────────────
        // Must match the table above exactly so it visually attaches
        PdfPTable totalTable = new PdfPTable(4);
        totalTable.setWidthPercentage(100);
        totalTable.setWidths(new float[]{2f, 2.5f, 1.8f, 2.5f});
        totalTable.setSpacingBefore(0);
        totalTable.setSpacingAfter(0);

        // "Total Expenses" label spans first 3 columns
        PdfPCell labelCell = new PdfPCell(new Phrase("Total Expenses", totalFont));
        labelCell.setColspan(3);
        labelCell.setBackgroundColor(new Color(220, 220, 220));
        labelCell.setPadding(8);
        labelCell.setBorder(Rectangle.BOX);
        labelCell.setBorderWidth(1.0f);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        
        PdfPCell valueCell = new PdfPCell(
                new Phrase(String.format("%.2f", getTotalExpenses(userId)), totalFont));
        valueCell.setBackgroundColor(accentColor);
        valueCell.setPadding(8);
        valueCell.setBorder(Rectangle.BOX);
        valueCell.setBorderWidth(1.0f);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        totalTable.addCell(labelCell);
        totalTable.addCell(valueCell);
        document.add(totalTable);

        document.close();
    }
}