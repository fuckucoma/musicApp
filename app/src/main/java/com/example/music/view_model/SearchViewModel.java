package com.example.music.view_model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.music.models.Track;
import com.example.music.repository.TrackRepository;

import java.util.List;

public class SearchViewModel extends ViewModel {
    private TrackRepository trackRepository;
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public SearchViewModel() {
        trackRepository = TrackRepository.getInstance();
    }

    public LiveData<List<Track>> getSearchTracks() {
        return trackRepository.getSearchTracks();
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void searchTracks(String query) {
        trackRepository.searchTracks(query);
        // Аналогично, можно расширить репозиторий,
        // чтобы при ошибке postValue в errorMessage
    }
}
