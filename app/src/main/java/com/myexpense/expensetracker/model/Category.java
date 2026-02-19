package com.myexpense.expensetracker.model;

import java.util.UUID;

public class Category {

    private String id;
    private String name;
    private double monthlyBudget;

    // Constructor for new category
    public Category(String name, double monthlyBudget) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.monthlyBudget = monthlyBudget;
    }

    // Constructor for loading from CSV
    public Category(String id, String name, double monthlyBudget) {
        this.id = id;
        this.name = name;
        this.monthlyBudget = monthlyBudget;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getMonthlyBudget() { return monthlyBudget; }
    public void setName(String name) { this.name = name; }
    public void setMonthlyBudget(double monthlyBudget) { this.monthlyBudget = monthlyBudget; }
}
