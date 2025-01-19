package com.example.music.models;

public class DashboardData {
    private int users; // Количество пользователей
    private int tracks; // Количество треков
    private int complaints; // Количество жалоб
    private int reviews; // Количество отзывов

    // Конструктор
    public DashboardData(int users, int tracks, int complaints, int reviews) {
        this.users = users;
        this.tracks = tracks;
        this.complaints = complaints;
        this.reviews = reviews;
    }


    public int getUsers() {
        return users;
    }

    public void setUsers(int users) {
        this.users = users;
    }

    public int getTracks() {
        return tracks;
    }

    public void setTracks(int tracks) {
        this.tracks = tracks;
    }

    public int getComplaints() {
        return complaints;
    }

    public void setComplaints(int complaints) {
        this.complaints = complaints;
    }

    public int getReviews() {
        return reviews;
    }

    public void setReviews(int reviews) {
        this.reviews = reviews;
    }
}
