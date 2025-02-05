package com.example.music.models;

public class Review {
    private int id;
    private User user;
    private Track track;
    private String content;
    private int rating;
    private String userProfileImage;

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public Review(int id, User user, Track track, String content, int rating, String userProfileImage) {
        this.id = id;
        this.user = user;
        this.track = track;
        this.content = content;
        this.rating = rating;
        this.userProfileImage = userProfileImage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
