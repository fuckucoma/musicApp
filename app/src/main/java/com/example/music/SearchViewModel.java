package com.example.music;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.music.models.Track;

import java.util.List;

public class SearchViewModel extends ViewModel {
    private MutableLiveData<List<Track>> searchResults = new MutableLiveData<>();
    private String lastQuery = null;

    public LiveData<List<Track>> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<Track> results) {
        searchResults.postValue(results);
    }

    public String getLastQuery() {
        return lastQuery;
    }

    public void setLastQuery(String query) {
        lastQuery = query;
    }
}
