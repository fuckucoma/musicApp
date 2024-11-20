package com.example.music.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.Track;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchRepository {

    private final ApiService apiService;
    private final MutableLiveData<List<Track>> searchResults = new MutableLiveData<>();

    public SearchRepository() {
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<List<Track>> getSearchResults() {
        return searchResults;
    }

    public void searchTracks(String query) {
        apiService.searchTracks(query).enqueue(new Callback<List<Track>>() {
            @Override
            public void onResponse(Call<List<Track>> call, Response<List<Track>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchResults.postValue(response.body());
                } else {
                    searchResults.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<Track>> call, Throwable t) {
                searchResults.postValue(null);
            }
        });
    }
}
