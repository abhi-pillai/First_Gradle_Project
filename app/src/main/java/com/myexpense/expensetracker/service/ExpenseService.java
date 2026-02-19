package com.myexpense.expensetracker.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPageEvent;
import com.myexpense.expensetracker.model.Expense;
import com.myexpense.expensetracker.repository.ExpenseRepository;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;


import java.awt.Color;
import com.lowagie.text.pdf.BaseFont;

public class ExpenseService  {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    // ===============================
    // BASIC CRUD
    // ===============================

    public void addExpense(Expense expense) throws Exception {
        repository.save(expense);
    }

    public List<Expense> getAllExpenses() {
        return repository.loadAll();
    }

    public void deleteExpense(String id) throws Exception {
        List<Expense> expenses = repository.loadAll();

        List<Expense> updated = expenses.stream()
                .filter(e -> !e.getId().equals(id))
                .collect(Collectors.toList());

        overwriteAll(updated);
    }

    public void updateExpense(Expense updatedExpense) throws Exception {
        List<Expense> expenses = repository.loadAll();

        List<Expense> updated = expenses.stream()
                .map(e -> e.getId().equals(updatedExpense.getId()) ? updatedExpense : e)
                .collect(Collectors.toList());

        overwriteAll(updated);
    }

    private void overwriteAll(List<Expense> expenses) throws Exception {
        java.io.File file = new java.io.File("data/expenses.csv");

        try (com.opencsv.CSVWriter writer =
                     new com.opencsv.CSVWriter(new java.io.FileWriter(file))) {

            writer.writeNext(new String[]{
                    "id", "amount", "date", "category", "note", "paymentMethod"
            });

            for (Expense e : expenses) {
                writer.writeNext(new String[]{
                        e.getId(),
                        String.valueOf(e.getAmount()),
                        e.getDate().toString(),
                        e.getCategory(),
                        e.getNote(),
                        e.getPaymentMethod().name()
                });
            }
        }
    }

    // ===============================
    // TOTAL CALCULATIONS
    // ===============================

    public double getTotalExpenses() {
        return repository.loadAll()
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public double getTotalByCategory(String category) {
        return repository.loadAll()
                .stream()
                .filter(e -> e.getCategory().equalsIgnoreCase(category))
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public double getTotalByDateRange(LocalDate start, LocalDate end) {
        return repository.loadAll()
                .stream()
                .filter(e -> !e.getDate().isBefore(start) &&
                             !e.getDate().isAfter(end))
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public double getMonthlyTotal(YearMonth month) {
        return repository.loadAll()
                .stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(month))
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public double getDailyTotal(LocalDate date) {
        return repository.loadAll()
                .stream()
                .filter(e -> e.getDate().equals(date))
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    // ===============================
    // FILTERING
    // ===============================

    public List<Expense> filterByDateRange(LocalDate start, LocalDate end) {
        return repository.loadAll()
                .stream()
                .filter(e -> !e.getDate().isBefore(start) &&
                             !e.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    public List<Expense> filterByCategory(String category) {
        return repository.loadAll()
                .stream()
                .filter(e -> e.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<Expense> searchByKeyword(String keyword) {
        return repository.loadAll()
                .stream()
                .filter(e -> e.getNote() != null &&
                        e.getNote().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    // ===============================
    // EXPORT PDF
    // ===============================

    public void exportToPdf(String filePath) throws Exception {

    List<Expense> expenses = repository.loadAll();
    
    BaseFont baseFont = BaseFont.createFont(
    "src/main/resources/fonts/NotoSans_ExtraCondensed-Regular.ttf",
    BaseFont.IDENTITY_H,
    BaseFont.EMBEDDED
    );

    Document document = new Document(PageSize.A4, 40, 40, 60, 60);
    PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

    // --- Fonts ---
    Font italicFont = new Font(baseFont, 36, Font.ITALIC, Color.BLACK);
    Font tagfont = new Font(baseFont, 10, Font.ITALIC);
    Font titleFont    = new Font(Font.HELVETICA, 22, Font.BOLD,   new Color(33, 37, 41));
    Font subFont      = new Font(Font.TIMES_ROMAN, 10, Font.NORMAL, new Color(108, 117, 125));
    Font headerFont   = new Font(baseFont, 10, Font.BOLD,   Color.WHITE);
    Font cellFont     = new Font(baseFont, 10, Font.NORMAL, new Color(33, 37, 41));
    Font totalFont    = new Font(baseFont, 11, Font.BOLD,   new Color(33, 37, 41));

    // --- Colors ---
    Color rowEven     = new Color(245, 247, 250);  // light gray
    Color rowOdd      = Color.WHITE;
    Color accentColor = new Color(52, 152, 219);  // blue accent

    writer.setPageEvent(new PdfPageEvent() {
    @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            cb.setColorStroke(new Color(46, 90, 136));
            cb.setLineWidth(2f);
            cb.rectangle(
                20,                                        // x
                20,                                        // y
                document.getPageSize().getWidth() - 40,   // width
                document.getPageSize().getHeight() - 40   // height
            );
            cb.stroke();
        }
    @Override public void onOpenDocument(PdfWriter w, Document d) {}
    @Override public void onStartPage(PdfWriter w, Document d) {}
    @Override public void onCloseDocument(PdfWriter w, Document d) {}
    @Override public void onParagraph(PdfWriter w, Document d, float v) {}
    @Override public void onParagraphEnd(PdfWriter w, Document d, float v) {}
    @Override public void onChapter(PdfWriter w, Document d, float v, Paragraph p) {}
    @Override public void onChapterEnd(PdfWriter w, Document d, float v) {}
    @Override public void onSection(PdfWriter w, Document d, float v, int i, Paragraph p) {}
    @Override public void onSectionEnd(PdfWriter w, Document d, float v) {}
    @Override public void onGenericTag(PdfWriter w, Document d, Rectangle r, String s) {}
    });

    document.open();

    // ── Title block ──────────────────────────────────────────────────
    Paragraph maintitle = new Paragraph("TrackIt", italicFont);
    maintitle.setAlignment(Element.ALIGN_CENTER);
    maintitle.setSpacingAfter(10);
    document.add(maintitle);
    Paragraph tagline = new Paragraph("\"Track your money, own your day.\"", tagfont);
    tagline.setAlignment(Element.ALIGN_CENTER);
    tagline.setSpacingAfter(4);
    document.add(tagline);
    Paragraph title = new Paragraph("Expense Report", titleFont);
    title.setAlignment(Element.ALIGN_LEFT);
    title.setSpacingAfter(4);
    document.add(title);

    Paragraph sub = new Paragraph("Generated on:" + LocalDate.now(), subFont);
    sub.setSpacingBefore(3);
    sub.setAlignment(Element.ALIGN_LEFT);
    sub.setSpacingAfter(2);
    document.add(sub);

    

    // ── Expense table ────────────────────────────────────────────────
    PdfPTable table = new PdfPTable(4);
    table.setWidthPercentage(100);
    table.setWidths(new float[]{2f, 2.5f, 1.5f, 2f});
    table.setSpacingBefore(10);
    table.setSpacingAfter(10);

    // Header row
    String[] headers = {"Date", "Category", "Amount", "Payment Method"};
    for (String h : headers) {
        PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
        cell.setBackgroundColor(new Color(46, 90, 136));
        cell.setPadding(8);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(1.0f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
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
            String.format("₹%.2f", e.getAmount()),
            e.getPaymentMethod().toString()
        };

        for (int i = 0; i < values.length; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(values[i], cellFont));
            cell.setBackgroundColor(rowColor);
            cell.setPadding(7);
            cell.setBorder(Rectangle.BOX);
            cell.setBorderWidth(1.0f);
            cell.setHorizontalAlignment(i == 2 ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
            table.addCell(cell);
        }
    }

    document.add(table);

    // ── Total row ────────────────────────────────────────────────────
    PdfPTable totalTable = new PdfPTable(2);
    totalTable.setWidthPercentage(100);
    totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
    totalTable.setWidths(new float[]{3.5f, 1f});

    PdfPCell labelCell = new PdfPCell(new Phrase("Total Expenses", totalFont));
    labelCell.setBackgroundColor(Color.LIGHT_GRAY);
    labelCell.setPadding(8);
    labelCell.setBorder(Rectangle.BOX);
    labelCell.setBorderWidth(1.0f);
    labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);

    PdfPCell valueCell = new PdfPCell(
        new Phrase(String.format("₹%.2f", getTotalExpenses()), totalFont));
    valueCell.setBackgroundColor(accentColor);
    valueCell.setPadding(8);
    valueCell.setBorder(Rectangle.BOX);
    valueCell.setBorderWidth(1.0f);
    valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

    totalTable.addCell(labelCell);
    totalTable.addCell(valueCell);
    document.add(totalTable);

    document.close();
}
}
