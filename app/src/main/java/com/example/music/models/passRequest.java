package com.example.music.models;

public class passRequest {

    private String currentPassword;
    private String newPassword;

    public passRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

}
