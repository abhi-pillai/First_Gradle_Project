package com.myexpense.expensetracker.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPageEvent;
import com.myexpense.expensetracker.model.Expense;
import com.myexpense.expensetracker.model.Income;
import com.myexpense.expensetracker.repository.IncomeRepository;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import java.awt.Color;
import com.lowagie.text.pdf.BaseFont;

public class IncomeService {

    private final IncomeRepository repository;

    public IncomeService(IncomeRepository repository) {
        this.repository = repository;
    }

    // ===============================
    // CRUD
    // ===============================
    public void addIncome(Income income) throws Exception {
        repository.save(income);
    }

    public List<Income> getAllIncome() {
        return repository.loadAll();
    }

    public void deleteIncome(String id) throws Exception {
        List<Income> incomes = repository.loadAll();
        List<Income> updated = incomes.stream()
                .filter(i -> !i.getId().equals(id))
                .collect(Collectors.toList());
        repository.overwriteAll(updated);
    }

    public void updateIncome(Income updatedIncome) throws Exception {
        List<Income> incomes = repository.loadAll();
        List<Income> updated = incomes.stream()
                .map(i -> i.getId().equals(updatedIncome.getId()) ? updatedIncome : i)
                .collect(Collectors.toList());
        repository.overwriteAll(updated);
    }

    // ===============================
    // TOTAL CALCULATIONS
    // ===============================
    public double getTotalIncome() {
        return repository.loadAll().stream().mapToDouble(Income::getAmount).sum();
    }

    public double getTotalBySource(String source) {
        return repository.loadAll().stream()
                .filter(i -> i.getSource().equalsIgnoreCase(source))
                .mapToDouble(Income::getAmount)
                .sum();
    }

    public double getTotalByDateRange(LocalDate start, LocalDate end) {
        return repository.loadAll().stream()
                .filter(i -> !i.getDate().isBefore(start) && !i.getDate().isAfter(end))
                .mapToDouble(Income::getAmount)
                .sum();
    }

    public double getMonthlyTotal(YearMonth month) {
        return repository.loadAll().stream()
                .filter(i -> YearMonth.from(i.getDate()).equals(month))
                .mapToDouble(Income::getAmount)
                .sum();
    }

    public double getDailyTotal(LocalDate date) {
        return repository.loadAll().stream()
                .filter(i -> i.getDate().equals(date))
                .mapToDouble(Income::getAmount)
                .sum();
    }

    // ===============================
    // FILTERING & SEARCH
    // ===============================
    public List<Income> filterByDateRange(LocalDate start, LocalDate end) {
        return repository.loadAll().stream()
                .filter(i -> !i.getDate().isBefore(start) && !i.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    public List<Income> filterBySource(String source) {
        return repository.loadAll().stream()
                .filter(i -> i.getSource().equalsIgnoreCase(source))
                .collect(Collectors.toList());
    }

    public List<Income> searchByKeyword(String keyword) {
        return repository.loadAll().stream()
                .filter(i -> i.getNote() != null &&
                        i.getNote().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    // ===============================
    // EXPORT PDF
    // ===============================
    public void exportToPdf(String filePath) throws Exception {

        List<Income> incomes = repository.loadAll();
        
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
        Paragraph title = new Paragraph("Income Report", titleFont);
        title.setAlignment(Element.ALIGN_LEFT);
        title.setSpacingAfter(4);
        document.add(title);

        Paragraph sub = new Paragraph("Generated on:" + LocalDate.now(), subFont);
        sub.setSpacingBefore(3);
        sub.setAlignment(Element.ALIGN_LEFT);
        sub.setSpacingAfter(2);
        document.add(sub);

        

        // ── Expense table ────────────────────────────────────────────────
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 2.5f, 1.5f});
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        // Header row
        String[] headers = {"Date", "Source", "Amount"};
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
        for (Income e : incomes) {
            Color rowColor = even ? rowEven : rowOdd;
            even = !even;

            String[] values = {
                e.getDate().toString(),
                e.getSource(),
                String.format("₹%.2f", e.getAmount()),
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

        PdfPCell labelCell = new PdfPCell(new Phrase("Total Income ", totalFont));
        labelCell.setBackgroundColor(Color.LIGHT_GRAY);
        labelCell.setPadding(8);
        labelCell.setBorder(Rectangle.BOX);
        labelCell.setBorderWidth(1.0f);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell valueCell = new PdfPCell(
            new Phrase(String.format("₹%.2f", getTotalIncome()), totalFont));
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

