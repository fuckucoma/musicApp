package com.example.music.models;

import com.google.gson.annotations.SerializedName;

public class Track {
    private int id;
    private String title;
    private String artist;
    private String album;
    private String imageUrl;
    private String filename;
    private String createdAt;

    // Конструктор без аргументов (по умолчанию)
    public Track() {
    }

    // Полный конструктор для удобства
    public Track(int id, String title, String artist, String album, String imageUrl, String filename, String createdAt) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.imageUrl = imageUrl;
        this.filename = filename;
        this.createdAt = createdAt;
    }

    // Геттеры
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getFilename() {
        return filename;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // Сеттеры
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
