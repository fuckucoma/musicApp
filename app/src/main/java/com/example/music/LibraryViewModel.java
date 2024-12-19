package com.example.music;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.music.models.Track;
import com.example.music.repository.FavoriteRepository;
import com.example.music.repository.LibraryRepository;
import com.example.music.repository.TrackRepository;

import java.util.List;

public class LibraryViewModel extends ViewModel {
    private TrackRepository trackRepository;
    private FavoriteRepository favoriteRepository;
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LibraryViewModel() {
        trackRepository = TrackRepository.getInstance();
        favoriteRepository = FavoriteRepository.getInstance(MyApp.getInstance());
    }

    public LiveData<List<Track>> getLibraryTracks() {
        return trackRepository.getLibraryTracks();
    }

    public LiveData<List<Integer>> getFavoriteTrackIds() {
        return favoriteRepository.getFavoriteTrackIds();
    }

    // Загрузка библиотеки (избранных) треков
    public void loadLibraryTracks() {
        trackRepository.loadLibraryTracks();
    }

    public void addToFavorites(Track track) {
        favoriteRepository.addTrackToFavorites(track);
    }

    public void removeFromFavorites(Track track) {
        favoriteRepository.removeTrackFromFavorites(track);
    }

    public boolean isTrackFavorite(int trackId) {
        return favoriteRepository.isTrackFavorite(trackId);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}