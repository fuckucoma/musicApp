package com.example.music.request;

public class ComplaintRequest {
    private String status;
    private String message;  // то, что мы реально хотим отправлять

    // Если нужен конструктор с "status", оставьте его отдельно
    public ComplaintRequest(String status) {
        this.status = status;
    }

    // Этот конструктор вызывается в AddComplaintDialog
    public ComplaintRequest(int trackId, String message) {
        this.message = message;
        // trackId тоже можно сохранить, если нужно
        // this.trackId = trackId;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}