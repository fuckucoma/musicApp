package com.example.music.request;

public class ReviewRequest {
    private int trackId;
    private String content;
    private int rating;// Новое поле

    public ReviewRequest(int trackId, String content, int rating) {
        this.trackId = trackId;
        this.content = content;
        this.rating = rating;
    }

    // Геттеры
    public int getTrackId() {
        return trackId;
    }

    public String getContent() {
        return content;
    }

    public int getRating() {
        return rating;
    }
}
