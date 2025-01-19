package com.example.music.models;

public class Complaint {
    private int id;
    private User user; // Модель User должна быть создана
    private String message;
    private String status;

    // Конструкторы, геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
