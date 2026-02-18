package com.myexpense.expensetracker.model;

import java.time.LocalDate;

public class Expense {

    private double amount;
    private LocalDate date;
    private String category;
    private String note;
    private String paymentMethod;

    public Expense(double amount, LocalDate date, String category, String note, String paymentMethod) {
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.note = note;
        this.paymentMethod = paymentMethod;
    }

    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getCategory() { return category; }
    public String getNote() { return note; }
    public String getPaymentMethod() { return paymentMethod; }
}
