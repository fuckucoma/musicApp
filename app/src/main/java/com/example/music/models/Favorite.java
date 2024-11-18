package com.example.music.models;

public class Favorite {
    private int id;
    private int trackId;
    private int userId;
    private String createdAt;
    private String updatedAt;


    public Favorite(int id, int trackId, int userId, String createdAt, String updatedAt) {
        this.id = id;
        this.trackId = trackId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Favorite() {
    }

    public int getId() {
        return id;
    }

    public int getTrackId() {
        return trackId;
    }

    public int getUserId() {
        return userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
