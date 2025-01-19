// UserFragment.java
package com.example.music.fragments;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.music.activity.LoginActivity;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.request.passRequest;
import com.example.music.request.usernameRequest;
import com.example.music.response.RegisterResponse;
import com.example.music.response.UserProfileResponse;
import com.example.test.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = "UserFragment";

    private ImageView ivProfilePicture;
    private TextView tvUsername;
    private ApiService apiService;
    private String authToken;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvUsername = view.findViewById(R.id.tvUsername);
        Button btnUploadImage = view.findViewById(R.id.btnUploadImage);
        EditText etCurrentPassword = view.findViewById(R.id.et_current_password);
        EditText etNewPassword = view.findViewById(R.id.et_new_password);
        EditText etNewUsername = view.findViewById(R.id.et_new_username);
        Button btnChangePassword = view.findViewById(R.id.btn_change_password);
        Button btnChangeUsername = view.findViewById(R.id.btn_change_username);

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

        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();

            if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(currentPassword, newPassword);
        });

        btnChangeUsername.setOnClickListener(v -> {
            String newUsername = etNewUsername.getText().toString();

            if (newUsername.isEmpty()) {
                Toast.makeText(getContext(), "Введите новое имя пользователя", Toast.LENGTH_SHORT).show();
                return;
            }

            changeUsername(newUsername);
        });

        return view;
    }

    private void changePassword(String currentPassword, String newPassword) {
        apiService.editPassword(new passRequest(currentPassword, newPassword)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Пароль успешно изменен", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Ошибка изменения пароля", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeUsername(String newUsername) {
        apiService.editUsername(new usernameRequest(newUsername)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Имя пользователя успешно изменено", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Ошибка изменения имени пользователя", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("authToken");
        editor.remove("isAdmin");
        editor.apply();

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
                ivProfilePicture.setImageBitmap(bitmap);
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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("profileImage", "profile.jpg", requestFile);

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
