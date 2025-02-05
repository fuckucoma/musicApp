package com.example.music.repository;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.Review;
import com.example.music.request.ReviewRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewRepository {

    private static ReviewRepository instance;
    private ApiService apiService;
    private MutableLiveData<List<Review>> reviewsLiveData = new MutableLiveData<>();

    private ReviewRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public static synchronized ReviewRepository getInstance() {
        if (instance == null) {
            instance = new ReviewRepository();
        }
        return instance;
    }

    public LiveData<List<Review>> getReviewsLiveData() {
        return reviewsLiveData;
    }


    public void createReview(ReviewRequest request) {
        Call<Void> call = apiService.createReview(request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {

                    fetchReviewsForTrack(request.getTrackId());

                } else {

                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }

    public void fetchReviewsForTrack(int trackId) {
        Call<List<Review>> call = apiService.getReviewsForTrack(trackId);
        call.enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reviewsLiveData.postValue(response.body());
                } else {
                    reviewsLiveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                reviewsLiveData.postValue(new ArrayList<>());
            }
        });
    }

    public void deleteUserReview(int reviewId, MyCallback<Void> callback) {
        apiService.deleteUserReview(reviewId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Успешно удалили
                    callback.onSuccess(null);
                } else {
                    // Сервер вернул ошибку (например, 403, 404...)
                    callback.onError(new Throwable("Ошибка при удалении отзыва (код " + response.code() + ")"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    // Интерфейс callback для обработки ответа
    public interface MyCallback<T> {
        void onSuccess(T data);
        void onError(Throwable t);
    }
}

