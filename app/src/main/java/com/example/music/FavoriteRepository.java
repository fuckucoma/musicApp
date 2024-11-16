package com.example.music;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.FavoriteResponse;
import com.example.music.models.Track;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteRepository {

    private final ApiService apiService;
    private final MutableLiveData<List<Integer>> favoriteTrackIds = new MutableLiveData<>(new ArrayList<>());

    public FavoriteRepository(Context context) {
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<List<Integer>> getFavoriteTrackIds() {
        return favoriteTrackIds;
    }

    public void refreshFavorites() {
        fetchFavorites(); // Повторно загружаем избранное с сервера
    }


    public void fetchFavorites() {
        apiService.getFavorites().enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Integer> trackIds = new ArrayList<>();
                    for (FavoriteResponse.FavoriteTrack favoriteTrack : response.body().getFavoriteTracks()) {
                        trackIds.add(favoriteTrack.getTrackId());
                    }
                    favoriteTrackIds.postValue(trackIds); // Уведомляем об изменении
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
                    List<Integer> currentFavorites = new ArrayList<>(favoriteTrackIds.getValue());
                    currentFavorites.add(track.getId());
                    favoriteTrackIds.postValue(currentFavorites);
                    fetchFavorites();
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
                    List<Integer> currentFavorites = new ArrayList<>(favoriteTrackIds.getValue());
                    currentFavorites.remove((Integer) track.getId());
                    favoriteTrackIds.postValue(currentFavorites); // Уведомляем об изменении
                    fetchFavorites();
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

    // Проверить, находится ли трек в избранном
    public boolean isTrackFavorite(int trackId) {
        List<Integer> favorites = favoriteTrackIds.getValue();
        return favorites != null && favorites.contains(trackId);
    }
}
