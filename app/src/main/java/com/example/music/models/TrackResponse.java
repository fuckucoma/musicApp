package com.example.music.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TrackResponse {

    @SerializedName("data")
    private List<TrackData> data;

    public List<TrackData> getData() {
        return data;
    }

    public static class TrackData {
        @SerializedName("id")
        private int id;

        @SerializedName("title")
        private String title;

        @SerializedName("artist")
        private String artist;

        @SerializedName("album")
        private String album;

        @SerializedName("imageUrl")
        private String imageUrl;

        @SerializedName("filename")
        private String filename;

        @SerializedName("createdAt")
        private String createdAt;

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
    }
}
