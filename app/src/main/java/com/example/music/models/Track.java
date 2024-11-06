package com.example.music.models;

public class Track {
    private final String id;
    private final String title;
    private final String artist;
    private final String album;
    private final String imageUrl;
    private final String filename;
    private final String createdAt;

    public Track(String id, String title, String artist, String album, String imageUrl, String filename, String createdAt) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.imageUrl = imageUrl;
        this.filename = filename;
        this.createdAt = createdAt;
    }

    public String getId() {
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
}
