package com.example.music.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.music.request.FavoriteRequest;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.response.FavoriteResponse;
import com.example.music.models.Track;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteRepository {
    private static FavoriteRepository instance;
    private final ApiService apiService;
    private final MutableLiveData<List<Integer>> favoriteTrackIds = new MutableLiveData<>(new ArrayList<>());

    private FavoriteRepository(Context context) {
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public static synchronized FavoriteRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FavoriteRepository(context.getApplicationContext());
        }
        return instance;
    }

    public LiveData<List<Integer>> getFavoriteTrackIds() {
        return favoriteTrackIds;
    }

    public void fetchFavorites() {
        // Так как getLibraryTracks() возвращает избранные треки, используем его
        apiService.getLibraryTracks().enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FavoriteResponse.FavoriteTrack> favoriteTracks = response.body().getFavoriteTracks();
                    List<Integer> trackIds = new ArrayList<>();
                    for (FavoriteResponse.FavoriteTrack ft : favoriteTracks) {
                        trackIds.add(ft.getTrackId());
                    }
                    favoriteTrackIds.postValue(trackIds);
                    Log.d("FavoriteRepository", "Fetched favorites: " + trackIds.size());
                } else {
                    Log.e("FavoriteRepository", "Ошибка загрузки избранного: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                Log.e("FavoriteRepository", "Ошибка сети: " + t.getMessage());
            }
        });
    }

    public void addTrackToFavorites(Track track) {
        apiService.addFavorite(new FavoriteRequest(track.getId())).enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful()) {
                    fetchFavorites();
                    Log.d("FavoriteRepository", "Track added to favorites: " + track.getTitle());
                } else {
                    Log.e("FavoriteRepository", "Ошибка добавления в избранное: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                Log.e("FavoriteRepository", "Ошибка сети: " + t.getMessage());
            }
        });
    }

    public void removeTrackFromFavorites(Track track) {
        apiService.removeFavorite(new FavoriteRequest(track.getId())).enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful()) {
                    fetchFavorites();
                    Log.d("FavoriteRepository", "Track removed from favorites: " + track.getTitle());
                } else {
                    Log.e("FavoriteRepository", "Ошибка удаления из избранного: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                Log.e("FavoriteRepository", "Ошибка сети: " + t.getMessage());
            }
        });
    }

    public boolean isTrackFavorite(int trackId) {
        List<Integer> favorites = favoriteTrackIds.getValue();
        return favorites != null && favorites.contains(trackId);
    }
}
