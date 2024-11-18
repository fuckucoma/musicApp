// UserFragment.java
package com.example.music.User_fragments;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.music.LoginActivity;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.RegisterResponse;
import com.example.music.models.UserProfileResponse;
import com.example.test.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserFragment extends Fragment {

    private static final String TAG = "UserFragment";

    private ImageView ivProfilePicture;
    private TextView tvUsername;
    private ApiService apiService;
    private String authToken;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvUsername = view.findViewById(R.id.tvUsername);
        Button btnUploadImage = view.findViewById(R.id.btnUploadImage);

        apiService = ApiClient.getClient().create(ApiService.class);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnLogout=view.findViewById(R.id.btnLogout);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", null);
        Log.d(TAG, "Token user: " + authToken);

        if (authToken != null) {
            fetchUserProfile();
        } else {
            Toast.makeText(getActivity(), "Пользователь не авторизован", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }

        btnUploadImage.setOnClickListener(v -> openImageChooser());

        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void logoutUser() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("authToken");
        editor.apply();

        // Перейти к экрану входа
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == getActivity().RESULT_OK && data != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                // Отображение выбранного изображения
                ivProfilePicture.setImageBitmap(bitmap);
                // Загрузка изображения на сервер
                uploadProfileImage(bitmap);
            } catch (IOException e) {
                Log.e(TAG, "Error selecting image: " + e.getMessage());
                Toast.makeText(getActivity(), "Ошибка при выборе изображения", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void uploadProfileImage(Bitmap bitmap) {
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(getActivity(), "Токен не найден. Пожалуйста, войдите снова.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
            return;
        }

        // Преобразование изображения в байты
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();

        // Создание RequestBody
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("profileImage", "profile.jpg", requestFile);

        // Вызов API без передачи токена, так как он добавляется автоматически через Interceptor
        Call<RegisterResponse> call = apiService.uploadProfileImage(body);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    RegisterResponse uploadResponse = response.body();
                    if (uploadResponse != null) {
                        Toast.makeText(getActivity(), "Изображение профиля обновлено", Toast.LENGTH_SHORT).show();
                        // Обновите данные профиля
                        fetchUserProfile();
                    }
                } else {
                    Log.e(TAG, "Failed to upload image: " + response.message());
                    Toast.makeText(getActivity(), "Не удалось загрузить изображение", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(getActivity(), "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserProfile() {
        Call<UserProfileResponse> call = apiService.getUserProfile();

        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful()) {
                    UserProfileResponse userProfile = response.body();
                    if (userProfile != null) {
                        Log.d(TAG, "User profile fetched successfully: " + userProfile);
                        populateUserData(userProfile);
                    }
                } else {
                    Log.e(TAG, "Failed to fetch user profile: " + response.message());
                    Toast.makeText(getActivity(), "Не удалось загрузить профиль", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(getActivity(), "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUserData(UserProfileResponse userProfile) {
        tvUsername.setText(userProfile.getUsername());

        Log.d(TAG,"USER: " + userProfile);
        Log.d(TAG,"USER Image: " + userProfile.getProfileImageUrl());


        if (userProfile.getProfileImageUrl() != null && !userProfile.getProfileImageUrl().isEmpty()) {
            String imageUrl = userProfile.getProfileImageUrl();
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_profile_24)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Кэширование для всех
                    .into(ivProfilePicture);
        } else {
            ivProfilePicture.setImageResource(R.drawable.ic_profile_24);
        }
    }

    private String getAuthToken() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("authToken", null);
    }
}
