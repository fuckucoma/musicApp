package com.example.music;

import android.app.Application;

import com.example.music.api.ApiClient;
import com.example.music.repository.FavoriteRepository;

public class MyApp extends Application {
    private static MyApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.initialize(this);
        instance = this; // сохраняем ссылку на экземпляр
    }

    public static MyApp getInstance() {
        return instance;
    }
}
