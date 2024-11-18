package com.example.music.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FavoriteResponse {
    private boolean success;
    private String message;
    @SerializedName("favorites")
    private List<FavoriteTrack> favoriteTracks;
    private String createdAt;

    public String getCreatedAt() {
        return createdAt;
    }

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

    public List<FavoriteTrack> getFavoriteTracks() {
        return favoriteTracks;
    }

    public void setFavoriteTracks(List<FavoriteTrack> favoriteTracks) {
        this.favoriteTracks = favoriteTracks;
    }

    // Вложенный класс для модели избранного трека
    public static class FavoriteTrack {
        private int favoriteId; // ID записи избранного
        private int trackId;    // ID самого трека
        private String title;
        private String artist;
        private String imageUrl;
        private String filename;
        private String createdAt;
        private String updatedAt;

        // Геттеры и сеттеры
        public int getFavoriteId() {
            return favoriteId;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
        public String getCreatedAt() {
            return createdAt;
        }

        public void setFavoriteId(int favoriteId) {
            this.favoriteId = favoriteId;
        }

        public int getTrackId() {
            return trackId;
        }

        public void setTrackId(int trackId) {
            this.trackId = trackId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getArtist() {
            return artist;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}