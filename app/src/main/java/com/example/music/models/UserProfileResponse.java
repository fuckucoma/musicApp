package com.example.music.models;

public class UserProfileResponse {
    private int id;
    private String username;
    private String email;
    private String profileImageUrl;
    private String createdAt;
    private String updatedAt;
    private boolean isAdmin;

    public int getId() {
        return id;
    }

    public void setAdmin(boolean isAdmin)
    {

    }

    public boolean getAdmin() {
        return isAdmin;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
