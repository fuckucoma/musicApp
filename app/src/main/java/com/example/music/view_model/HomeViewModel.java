package com.example.music.view_model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.music.models.Track;
import com.example.music.repository.TrackRepository;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private TrackRepository trackRepository;
    private MutableLiveData<List<Track>> feedTracksLiveData = new MutableLiveData<>();
    private boolean isDataLoaded = false; // Флаг для отслеживания загрузки данных

    public HomeViewModel() {
        trackRepository = TrackRepository.getInstance();
    }

    public LiveData<List<Track>> getFeedTracks() {
        return feedTracksLiveData;
    }

    public void loadFeedTracks() {
        if (isDataLoaded) {
            // Данные уже загружены, не нужно повторно загружать
            return;
        }

        trackRepository.getFeedTracks().observeForever(tracks -> {
            if (tracks != null && !tracks.isEmpty()) {
                feedTracksLiveData.postValue(tracks);
                isDataLoaded = true;
                Log.d("HomeViewModel", "Feed tracks loaded: " + tracks.size());
            } else {
                Log.e("HomeViewModel", "Ошибка загрузки ленты или пустой список");
            }
        });

        // Инициируем загрузку треков из репозитория
        trackRepository.loadFeedTracks();
    }
}
