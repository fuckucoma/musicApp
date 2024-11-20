package com.example.music.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.music.FavoriteRequest;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.response.FavoriteResponse;
import com.example.music.models.Track;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    public void fetchFavorites() {
        apiService.getFavorites().enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {


                    List<FavoriteResponse.FavoriteTrack> favoriteTracks = response.body().getFavoriteTracks();

                    // Сортируем список треков по createdAt
                    Collections.sort(favoriteTracks, (track1, track2) -> {
                        // Преобразуем строки createdAt в тип Date, если они в формате, поддерживающем сравнение
                        return track2.getCreatedAt().compareTo(track1.getCreatedAt()); // Отсортируем по убыванию
                    });


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

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        return sdf.format(new Date()); // Возвращает строку в формате ISO 8601
    }

    public void addTrackToFavorites(Track track) {
        apiService.addFavorite(new FavoriteRequest(track.getId())).enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful()) {
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
                    List<FavoriteResponse.FavoriteTrack> favoriteTracks = response.body().getFavoriteTracks();

                    List<Integer> currentFavorites = new ArrayList<>(favoriteTrackIds.getValue());
                    currentFavorites.remove((Integer) track.getId());
                    favoriteTrackIds.postValue(currentFavorites);
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
