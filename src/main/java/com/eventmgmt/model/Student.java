package com.eventmgmt.model;

/**
 * Student POJO – mirrors the `students` table in SQLite.
 * Email must be unique (enforced by the DB schema).
 */
public class Student {

    private int    id;
    private String name;
    private String email;
    private String createdAt;

    // --- Getters ---

    public int    getId()        { return id; }
    public String getName()      { return name; }
    public String getEmail()     { return email; }
    public String getCreatedAt() { return createdAt; }

    // --- Setters ---

    public void setId(int id)                  { this.id = id; }
    public void setName(String name)           { this.name = name; }
    public void setEmail(String email)         { this.email = email; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}
