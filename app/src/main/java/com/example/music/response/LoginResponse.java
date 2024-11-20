package com.example.music.response;

public class LoginResponse {

    private String message;

    private String token;

    private boolean admin;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean getAdmin(){
        return admin;
    }
}
