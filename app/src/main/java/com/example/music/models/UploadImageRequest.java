package com.example.music.models;

public class UploadImageRequest {
    private String imageBase64; // Изображение в формате Base64

    public UploadImageRequest(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}
