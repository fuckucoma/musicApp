package com.example.music.repository;

import androidx.lifecycle.ViewModelProvider;

import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.Track;
import com.example.music.request.ComplaintRequest;
import com.example.music.request.ReviewRequest;
import com.example.music.view_model.PlayerViewModel;

import retrofit2.Call;
import retrofit2.Response;

public class RevComRepository {
    private static RevComRepository instance;
    private final ApiService apiService;

    private RevComRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public static synchronized RevComRepository getInstance() {
        if (instance == null) {
            instance = new RevComRepository();
        }
        return instance;
    }

    public interface MyCallback<T> {
        void onSuccess(T data);
        void onError(Throwable t);
    }

//    public void createReview(ReviewRequest request, MyCallback<Void> callback) {
//        apiService.createReview(request).enqueue(new retrofit2.Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                if (response.isSuccessful()) {
//                    callback.onSuccess(null);
//                } else {
//                    callback.onError(new Throwable("Ошибка при добавлении отзыва"));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                callback.onError(t);
//            }
//        });
//    }

    public void createComplaint(ComplaintRequest request, MyCallback<Void> callback) {
        apiService.createComplaint(request).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(new Throwable("Ошибка при отправке жалобы"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}
