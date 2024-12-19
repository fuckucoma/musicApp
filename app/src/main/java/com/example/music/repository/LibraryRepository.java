package com.example.music.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.Track;
import com.example.music.response.FavoriteResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryRepository {

    private final ApiService apiService;
    private final MutableLiveData<List<Track>> libraryTracks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Integer>> favoriteTrackIds = new MutableLiveData<>(new ArrayList<>());

    public LibraryRepository(Context context) {
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<List<Track>> getLibraryTracks() {
        return libraryTracks;
    }

    public LiveData<List<Integer>> getFavoriteTrackIds() {
        return favoriteTrackIds;
    }

    public void fetchLibraryTracks() {
        apiService.getLibraryTracks().enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Track> tracks = new ArrayList<>();
                    List<Integer> trackIds = new ArrayList<>();
                    for (FavoriteResponse.FavoriteTrack favoriteTrack : response.body().getFavoriteTracks()) {
                        Track track = new Track();
                        track.setId(favoriteTrack.getTrackId());
                        track.setTitle(favoriteTrack.getTitle());
                        track.setArtist(favoriteTrack.getArtist());
                        track.setImageUrl(favoriteTrack.getImageUrl());
                        track.setFilename(favoriteTrack.getFilename());
                        track.setCreatedAt(favoriteTrack.getCreatedAt());
                        tracks.add(track);
                        trackIds.add(favoriteTrack.getTrackId());
                    }
                    libraryTracks.postValue(tracks);
                    favoriteTrackIds.postValue(trackIds);
                } else {
                    Log.e("LibraryRepository", "Ошибка получения избранных треков: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                Log.e("LibraryRepository", "Ошибка сети: " + t.getMessage());
            }
        });
    }
}

