package com.myexpense.expensetracker.model;

import java.util.UUID;

public class User {

    private String id;
    private String username;
    private String passwordHash;
    private String salt;

    // Constructor for new user
    public User(String username, String passwordHash, String salt) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    // Constructor for loading from CSV
    public User(String id, String username, String passwordHash, String salt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public String getId()           { return id; }
    public String getUsername()     { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getSalt()         { return salt; }

    public void setUsername(String username)         { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setSalt(String salt)                 { this.salt = salt; }
}