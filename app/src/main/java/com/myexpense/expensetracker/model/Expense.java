package com.myexpense.expensetracker.model;

import java.time.LocalDate;
import java.util.UUID;

public class Expense {

    private String id;
    private String userId;
    private double amount;
    private LocalDate date;
    private String category;
    private String note;
    private PaymentMethod paymentMethod;

    // Constructor for new expense
    public Expense(String userId, double amount, LocalDate date, String category, String note, PaymentMethod paymentMethod) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.note = note;
        this.paymentMethod = paymentMethod;
    }

    // Constructor for loading from CSV
    public Expense(String id, String userId, double amount, LocalDate date, String category, String note, PaymentMethod paymentMethod) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.note = note;
        this.paymentMethod = paymentMethod;
    }

    public String getId()                  { return id; }
    public String getUserId()              { return userId; }
    public double getAmount()              { return amount; }
    public LocalDate getDate()             { return date; }
    public String getCategory()            { return category; }
    public String getNote()                { return note; }
    public PaymentMethod getPaymentMethod(){ return paymentMethod; }
}