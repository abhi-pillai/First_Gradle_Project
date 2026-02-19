package com.myexpense.expensetracker.model;

import java.time.LocalDate;
import java.util.UUID;


public class Expense {

    private String id;
    private double amount;
    private LocalDate date;
    private String category;
    private String note;
    private PaymentMethod paymentMethod;

    public Expense(double amount, LocalDate date, String category, String note, PaymentMethod paymentMethod) {

        this.id = UUID.randomUUID().toString();
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.note = note;
        this.paymentMethod = paymentMethod;
    }

    public Expense(String id, double amount, LocalDate date, String category, String note, PaymentMethod paymentMethod) {

        this.id = id;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.note = note;
        this.paymentMethod = paymentMethod;
    }


    // getters
    public String getId() { return id; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getCategory() { return category; }
    public String getNote() { return note; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
}
