package com.example.music;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.music.models.Track;
import com.example.music.repository.LibraryRepository;

import java.util.List;

public class LibraryViewModel extends AndroidViewModel {

    private final LibraryRepository libraryRepository;

    public LibraryViewModel(Application application) {
        super(application);
        libraryRepository = new LibraryRepository(application);
        libraryRepository.fetchLibraryTracks();
    }

    public LiveData<List<Track>> getLibraryTracks() {
        return libraryRepository.getLibraryTracks();
    }

    public LiveData<List<Integer>> getFavoriteTrackIds() {
        return libraryRepository.getFavoriteTrackIds();
    }

    public void refreshLibraryTracks() {
        libraryRepository.fetchLibraryTracks();
    }
}
