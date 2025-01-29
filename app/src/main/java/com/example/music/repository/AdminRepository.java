package com.example.music.repository;

import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.Complaint;
import com.example.music.request.ComplaintRequest;
import com.example.music.models.DashboardData;
import com.example.music.models.Review;
import com.example.music.models.Track;
import com.example.music.request.TrackUpdateRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRepository {

    private static AdminRepository instance;
    private final ApiService apiService;

    private AdminRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public static synchronized AdminRepository getInstance() {
        if (instance == null) {
            instance = new AdminRepository();
        }
        return instance;
    }

    // ==== Пример универсального callback-интерфейса ====
    public interface MyCallback<T> {
        void onSuccess(T data);
        void onError(Throwable t);
    }

    // =================== ПАНЕЛЬ АДМИНИСТРАТОРА ===================
    public void fetchDashboardData(MyCallback<DashboardData> callback) {
        apiService.getDashboardData().enqueue(new Callback<DashboardData>() {
            @Override
            public void onResponse(Call<DashboardData> call, Response<DashboardData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Throwable("Ошибка загрузки данных панели"));
                }
            }

            @Override
            public void onFailure(Call<DashboardData> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    // =================== ЖАЛОБЫ ===================
    public void fetchComplaints(MyCallback<List<Complaint>> callback) {
        apiService.getAllComplaints().enqueue(new Callback<List<Complaint>>() {
            @Override
            public void onResponse(Call<List<Complaint>> call, Response<List<Complaint>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Throwable("Ошибка загрузки жалоб"));
                }
            }

            @Override
            public void onFailure(Call<List<Complaint>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void updateComplaintStatus(int complaintId, String status, MyCallback<Void> callback) {
        ComplaintRequest request = new ComplaintRequest(status);
        apiService.updateComplaint(complaintId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(new Throwable("Ошибка обновления статуса жалобы"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    // =================== ОТЗЫВЫ ===================
    public void fetchReviews(MyCallback<List<Review>> callback) {
        apiService.getAllReviews().enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Throwable("Ошибка загрузки отзывов"));
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void deleteReview(int reviewId, MyCallback<Void> callback) {
        apiService.deleteReview(reviewId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(new Throwable("Ошибка удаления отзыва"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    // =================== ТРЕКИ ===================
    public void fetchTracks(MyCallback<List<Track>> callback) {
        apiService.getAllTracks().enqueue(new Callback<List<Track>>() {
            @Override
            public void onResponse(Call<List<Track>> call, Response<List<Track>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Throwable("Ошибка загрузки треков"));
                }
            }

            @Override
            public void onFailure(Call<List<Track>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void updateTrack(int trackId, TrackUpdateRequest request, MyCallback<Void> callback) {
        apiService.updateTrack(trackId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(new Throwable("Ошибка обновления трека"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void deleteTrack(int trackId, MyCallback<Void> callback) {
        apiService.deleteTrack(trackId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(new Throwable("Ошибка удаления трека"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}
