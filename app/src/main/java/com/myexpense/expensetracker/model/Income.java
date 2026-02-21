package com.myexpense.expensetracker.model;

import java.time.LocalDate;
import java.util.UUID;

public class Income {

    private String id;
    private String userId;
    private double amount;
    private LocalDate date;
    private String source;
    private String note;

    // Constructor for new income
    public Income(String userId, double amount, LocalDate date, String source, String note) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.source = source;
        this.note = note;
    }

    // Constructor for loading from CSV
    public Income(String id, String userId, double amount, LocalDate date, String source, String note) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.source = source;
        this.note = note;
    }

    public String getId()      { return id; }
    public String getUserId()  { return userId; }
    public double getAmount()  { return amount; }
    public LocalDate getDate() { return date; }
    public String getSource()  { return source; }
    public String getNote()    { return note; }
}