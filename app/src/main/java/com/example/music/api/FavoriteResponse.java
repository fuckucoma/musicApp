package com.example.music.api;

import com.example.music.models.Track;

import java.util.List;

public class FavoriteResponse {
    private boolean success;
    private String message;
    private List<Track> favorites;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Track> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<Track> favorites) {
        this.favorites = favorites;
    }
}
