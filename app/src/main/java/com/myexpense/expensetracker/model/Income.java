package com.myexpense.expensetracker.model;

import java.time.LocalDate;
import java.util.UUID;
public class Income {

    private String id;
    private double amount;
    private LocalDate date;
    private String source;
    private String note;

    public Income(double amount, LocalDate date, String source, String note) {
        this.id = UUID.randomUUID().toString();
        this.amount = amount;
        this.date = date;
        this.source = source;
        this.note = note;
    }

    // getters
    public String getId() { return id; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getSource() { return source; }
    public String getNote() { return note; }
}

