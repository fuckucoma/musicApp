package com.example.music.models;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Track {
    private int id;
    private String title;
    private String artist;
    private String album;
    private String imageUrl;
    private String filename;
    private String createdAt;


    public Date getCreatedAtDate() {
        if (createdAt == null) return null;

        // Парсим строку в объект Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        try {
            return dateFormat.parse(createdAt);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Если ошибка парсинга, возвращаем null
        }
    }

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
