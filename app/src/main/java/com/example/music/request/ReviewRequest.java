package com.example.music.request;

public class ReviewRequest {
    private int trackId;
    private String content;
    private int rating;
    private String userProfileImage; // Новое поле

    public ReviewRequest(int trackId, String content, int rating, String userProfileImage) {
        this.trackId = trackId;
        this.content = content;
        this.rating = rating;
        this.userProfileImage = userProfileImage;
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

    public String getUserProfileImage() {
        return userProfileImage;
    }
}
