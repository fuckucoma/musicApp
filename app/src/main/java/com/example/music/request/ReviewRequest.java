package com.example.music.request;

public class ReviewRequest {
    private int trackId;
    private String content;
    private int rating;

    public ReviewRequest(int trackId, String content, int rating) {
        this.trackId = trackId;
        this.content = content;
        this.rating = rating;
    }

    // getters / setters ...
}
