package com.myexpense.expensetracker.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.myexpense.expensetracker.model.Income;
import com.myexpense.expensetracker.repository.IncomeRepository;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<Income> getAllIncome(String userId) {
        return repository.loadByUser(userId);
    }

    public void deleteIncome(String userId, String id) throws Exception {
        List<Income> updated = repository.loadByUser(userId).stream()
                .filter(i -> !i.getId().equals(id))
                .collect(Collectors.toList());
        repository.overwriteForUser(userId, updated);
    }

    public void updateIncome(String userId, Income updatedIncome) throws Exception {
        List<Income> updated = repository.loadByUser(userId).stream()
                .map(i -> i.getId().equals(updatedIncome.getId()) ? updatedIncome : i)
                .collect(Collectors.toList());
        repository.overwriteForUser(userId, updated);
    }

    // ===============================
    // TOTALS
    // ===============================

    public double getTotalIncome(String userId) {
        return repository.loadByUser(userId).stream().mapToDouble(Income::getAmount).sum();
    }

    public double getTotalBySource(String userId, String source) {
        return repository.loadByUser(userId).stream()
                .filter(i -> i.getSource().equalsIgnoreCase(source))
                .mapToDouble(Income::getAmount).sum();
    }

    public double getTotalByDateRange(String userId, LocalDate start, LocalDate end) {
        return repository.loadByUser(userId).stream()
                .filter(i -> !i.getDate().isBefore(start) && !i.getDate().isAfter(end))
                .mapToDouble(Income::getAmount).sum();
    }

    public double getMonthlyTotal(String userId, YearMonth month) {
        return repository.loadByUser(userId).stream()
                .filter(i -> YearMonth.from(i.getDate()).equals(month))
                .mapToDouble(Income::getAmount).sum();
    }

    public double getDailyTotal(String userId, LocalDate date) {
        return repository.loadByUser(userId).stream()
                .filter(i -> i.getDate().equals(date))
                .mapToDouble(Income::getAmount).sum();
    }

    // ===============================
    // FILTERING & SEARCH
    // ===============================

    public List<Income> filterByDateRange(String userId, LocalDate start, LocalDate end) {
        return repository.loadByUser(userId).stream()
                .filter(i -> !i.getDate().isBefore(start) && !i.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    public List<Income> filterBySource(String userId, String source) {
        return repository.loadByUser(userId).stream()
                .filter(i -> i.getSource().equalsIgnoreCase(source))
                .collect(Collectors.toList());
    }

    public List<Income> searchByKeyword(String userId, String keyword) {
        String kw = keyword.toLowerCase();
        return repository.loadByUser(userId).stream()
                .filter(i -> (i.getNote() != null && i.getNote().toLowerCase().contains(kw))
                        || i.getSource().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    public List<Income> filterAdvanced(String userId, LocalDate start, LocalDate end,
                                       String source, String keyword) {
        return repository.loadByUser(userId).stream()
                .filter(i -> start == null || !i.getDate().isBefore(start))
                .filter(i -> end   == null || !i.getDate().isAfter(end))
                .filter(i -> source == null || source.isEmpty()
                        || i.getSource().equalsIgnoreCase(source))
                .filter(i -> keyword == null || keyword.isEmpty()
                        || (i.getNote() != null && i.getNote().toLowerCase().contains(keyword.toLowerCase()))
                        || i.getSource().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    // ===============================
    // EXPORT PDF
    // ===============================

    public void exportToPdf(String userId, String filePath) throws Exception {
        List<Income> incomes = repository.loadByUser(userId);

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

        

        // ── Income table ────────────────────────────────────────────────
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 2.5f, 1.5f});
        table.setSpacingBefore(0);
        table.setSpacingAfter(0);

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
        PdfPTable totalTable = new PdfPTable(3);
        totalTable.setWidthPercentage(100);
        totalTable.setWidths(new float[]{2f, 2.5f, 1.5f});
        totalTable.setSpacingBefore(0);
        totalTable.setSpacingAfter(0);

        PdfPCell labelCell = new PdfPCell(new Phrase("Total Income ", totalFont));
        labelCell.setColspan(2);
        labelCell.setBackgroundColor(new Color(220, 220, 220));
        labelCell.setPadding(8);
        labelCell.setBorder(Rectangle.BOX);
        labelCell.setBorderWidth(1.0f);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell valueCell = new PdfPCell(
            new Phrase(String.format("₹%.2f", getTotalIncome(userId)), totalFont));
        valueCell.setBackgroundColor(accentColor);
        valueCell.setPadding(8);
        valueCell.setBorder(Rectangle.BOX);
        valueCell.setBorderWidth(1.0f);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        totalTable.addCell(labelCell);
        totalTable.addCell(valueCell);
        document.add(totalTable);

        document.close();
    }
}