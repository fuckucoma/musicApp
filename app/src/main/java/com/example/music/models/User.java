package com.example.music.models;

public class User {
    private String username;
    private String password;
   //private String email;
    private String profileImageUrl;
    private int id;

    public int getId() {
        return id;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public User(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public String getProfileImageUrl(){
        return profileImageUrl;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    //public String getEmail() {
    //    return email;
    //}


    // public void setEmail(String email) {
    //    this.email = email;
    //}
}
