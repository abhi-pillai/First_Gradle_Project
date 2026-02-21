package com.myexpense.expensetracker.repository;

import com.myexpense.expensetracker.model.Income;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IncomeRepository {

    private static final String FILE_PATH = getDataPath("income.csv");

    private static String getDataPath(String filename) {
        String os   = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");
        String base;
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            base = (appData != null ? appData : home) + java.io.File.separator + "TrackIt" + java.io.File.separator + "data";
        } else if (os.contains("mac")) {
            base = home + "/Library/Application Support/TrackIt/data";
        } else {
            base = home + "/.local/share/TrackIt/data";
        }
        return base + java.io.File.separator + filename;
    }
    private static final String[] HEADER = {"id", "userId", "amount", "date", "source", "note"};

    public IncomeRepository() {
        initFile();
    }

    private void initFile() {
        try {
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
                writeHeader(file);
                return;
            }
            if (!hasValidHeader(file)) {
                prependHeader(file);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error initialising income file", e);
        }
    }

    private void writeHeader(File file) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            writer.writeNext(HEADER);
        }
    }

    private boolean hasValidHeader(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String firstLine = br.readLine();
            return firstLine != null && firstLine.contains("userId") && firstLine.contains("source");
        }
    }

    private void prependHeader(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("\"id\",\"userId\",\"amount\",\"date\",\"source\",\"note\"");
            for (String line : lines) pw.println(line);
        }
    }

    public void save(Income income) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
            writer.writeNext(toRow(income));
        }
    }

    public List<Income> loadByUser(String userId) {
        List<Income> incomes = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return incomes;

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean isFirstLine = true;
            while ((line = reader.readNext()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                if (line.length < 6) continue;
                if (!line[1].equals(userId)) continue;

                incomes.add(new Income(
                        line[0], line[1],
                        Double.parseDouble(line[2]),
                        LocalDate.parse(line[3]),
                        line[4], line[5]
                ));
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Error reading income file", e);
        }
        return incomes;
    }

    public void overwriteForUser(String userId, List<Income> updatedIncomes) throws IOException {
        List<Income> all = loadAll();
        List<Income> others = new ArrayList<>();
        for (Income i : all) {
            if (!i.getUserId().equals(userId)) others.add(i);
        }
        others.addAll(updatedIncomes);

        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
            writer.writeNext(HEADER);
            for (Income i : others) writer.writeNext(toRow(i));
        }
    }

    private List<Income> loadAll() {
        List<Income> incomes = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return incomes;
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean isFirstLine = true;
            while ((line = reader.readNext()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                if (line.length < 6) continue;
                incomes.add(new Income(
                        line[0], line[1],
                        Double.parseDouble(line[2]),
                        LocalDate.parse(line[3]),
                        line[4], line[5]
                ));
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Error reading income file", e);
        }
        return incomes;
    }

    private String[] toRow(Income i) {
        return new String[]{
                i.getId(), i.getUserId(),
                String.valueOf(i.getAmount()),
                i.getDate().toString(),
                i.getSource(), i.getNote()
        };
    }
}