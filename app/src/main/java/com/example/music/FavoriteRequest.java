package com.example.music;

public class FavoriteRequest {
    private int trackId;

    public FavoriteRequest(int trackId) {
        this.trackId = trackId;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }
}

