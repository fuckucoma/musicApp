package com.example.music.repository;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.response.RegisterResponse;
import com.example.music.response.UserProfileResponse;
import com.example.music.request.passRequest;
import com.example.music.request.usernameRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {

    private static final String TAG = "ProfileRepository";

    private static ProfileRepository instance;
    private final ApiService apiService;

    // LiveData с данными профиля
    private final MutableLiveData<UserProfileResponse> userProfileLiveData = new MutableLiveData<>();

    // LiveData для отслеживания успеха/неуспеха операций
    private final MutableLiveData<Boolean> passwordChangeSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> usernameChangeSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> imageUploadSuccess = new MutableLiveData<>();

    // LiveData для ошибок (опционально)
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    private ProfileRepository() {
        // Инициализация ApiService через ваш ApiClient
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    // Реализуем паттерн Singleton, чтобы во всем приложении был один репозиторий
    public static synchronized ProfileRepository getInstance() {
        if (instance == null) {
            instance = new ProfileRepository();
        }
        return instance;
    }

    // region Getters for LiveData

    public LiveData<UserProfileResponse> getUserProfileLiveData() {
        return userProfileLiveData;
    }

    public LiveData<Boolean> getPasswordChangeSuccess() {
        return passwordChangeSuccess;
    }

    public LiveData<Boolean> getUsernameChangeSuccess() {
        return usernameChangeSuccess;
    }

    public LiveData<Boolean> getImageUploadSuccess() {
        return imageUploadSuccess;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public void fetchUserProfile() {
        apiService.getUserProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userProfileLiveData.postValue(response.body());
                    Log.d(TAG, "User profile fetched: " + response.body().getUsername());
                } else {
                    Log.e(TAG, "Failed to fetch user profile: " + response.message());
                    errorMessageLiveData.postValue("Не удалось загрузить профиль");
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Log.e(TAG, "Network error while fetching profile: " + t.getMessage());
                errorMessageLiveData.postValue("Ошибка сети при загрузке профиля");
            }
        });
    }


    public void changePassword(String currentPassword, String newPassword) {
        apiService.editPassword(new passRequest(currentPassword, newPassword)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    passwordChangeSuccess.postValue(true);
                    Log.d(TAG, "Password changed successfully");
                } else {
                    passwordChangeSuccess.postValue(false);
                    Log.e(TAG, "Failed to change password: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                passwordChangeSuccess.postValue(false);
                Log.e(TAG, "Network error: " + t.getMessage());
            }
        });
    }


    public void changeUsername(String newUsername) {
        apiService.editUsername(new usernameRequest(newUsername)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    usernameChangeSuccess.postValue(true);
                    Log.d(TAG, "Username changed successfully");
                } else {
                    usernameChangeSuccess.postValue(false);
                    Log.e(TAG, "Failed to change username: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                usernameChangeSuccess.postValue(false);
                Log.e(TAG, "Network error: " + t.getMessage());
            }
        });
    }


    public void uploadProfileImage(Bitmap bitmap) {
        // Преобразуем Bitmap в массив байтов
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("profileImage", "profile.jpg", requestFile);

        apiService.uploadProfileImage(body).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    imageUploadSuccess.postValue(true);
                    Log.d(TAG, "Profile image uploaded: " + response.body().toString());
                    // После успешной загрузки можно заново запросить профиль:
                    fetchUserProfile();
                } else {
                    imageUploadSuccess.postValue(false);
                    Log.e(TAG, "Failed to upload image: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                imageUploadSuccess.postValue(false);
                Log.e(TAG, "Network error: " + t.getMessage());
            }
        });
    }

    // endregion
}