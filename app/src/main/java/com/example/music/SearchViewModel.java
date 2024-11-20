package com.example.music;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.music.models.Track;
import com.example.music.repository.SearchRepository;

import java.util.List;

public class SearchViewModel extends ViewModel {

    private final SearchRepository searchRepository;
    private String lastQuery = null;

    public SearchViewModel() {
        searchRepository = new SearchRepository();
    }

    public LiveData<List<Track>> getSearchResults() {
        return searchRepository.getSearchResults();
    }

    public void searchTracks(String query) {
        lastQuery = query;
        searchRepository.searchTracks(query);
    }

    public String getLastQuery() {
        return lastQuery;
    }
}
