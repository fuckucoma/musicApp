package com.example.music;

import android.app.Application;

import com.example.music.api.ApiClient;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.initialize(this);
    }
}
