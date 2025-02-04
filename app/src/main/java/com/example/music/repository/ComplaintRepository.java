package com.example.music.repository;

import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.request.ComplaintRequest;

import retrofit2.Call;
import retrofit2.Response;

public class ComplaintRepository {
    private static ComplaintRepository instance;
    private final ApiService apiService;

    private ComplaintRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public static synchronized ComplaintRepository getInstance() {
        if (instance == null) {
            instance = new ComplaintRepository();
        }
        return instance;
    }

    public interface MyCallback<T> {
        void onSuccess(T data);
        void onError(Throwable t);
    }

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
