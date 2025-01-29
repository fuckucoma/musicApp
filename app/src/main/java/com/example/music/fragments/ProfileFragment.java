// UserFragment.java
package com.example.music.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.music.activity.LoginActivity;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.request.passRequest;
import com.example.music.request.usernameRequest;
import com.example.music.response.RegisterResponse;
import com.example.music.response.UserProfileResponse;
import com.example.music.service.MusicService;
import com.example.music.view_model.ProfileViewModel;
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

    private static final String TAG = "ProfileFragment";

    private ProfileViewModel profileViewModel;

    private ImageView ivProfilePicture;
    private TextView tvUsername;
    private EditText etCurrentPassword, etNewPassword, etNewUsername;
    private Button btnUploadImage, btnChangePassword, btnChangeUsername, btnLogout;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvUsername = view.findViewById(R.id.tvUsername);

        etCurrentPassword = view.findViewById(R.id.et_current_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        etNewUsername = view.findViewById(R.id.et_new_username);

        btnUploadImage = view.findViewById(R.id.btnUploadImage);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnChangeUsername = view.findViewById(R.id.btn_change_username);
        btnLogout = view.findViewById(R.id.btnLogout);

        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        if (!hasValidToken()) {
            redirectToLogin();
        } else {
            profileViewModel.fetchUserProfile();
        }

        observeViewModel();


        btnUploadImage.setOnClickListener(v -> openImageChooser());
        btnChangePassword.setOnClickListener(v -> handleChangePassword());
        btnChangeUsername.setOnClickListener(v -> handleChangeUsername());
        btnLogout.setOnClickListener(v -> logoutUser());

        registerImagePickerCallback();

        return view;
    }


    private boolean hasValidToken() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String authToken = prefs.getString("authToken", null);
        Log.d(TAG, "AuthToken: " + authToken);
        return (authToken != null && !authToken.isEmpty());
    }

    private void redirectToLogin() {
        Toast.makeText(requireContext(), "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private void observeViewModel() {

        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                Log.d(TAG, "User profile fetched: " + userProfile.getUsername());
                populateUserData(userProfile);
            }
        });

        profileViewModel.getPasswordChangeSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(getContext(), "Пароль успешно изменен", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Ошибка изменения пароля", Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.getUsernameChangeSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(getContext(), "Имя пользователя успешно изменено", Toast.LENGTH_SHORT).show();
                profileViewModel.fetchUserProfile();
            } else {
                Toast.makeText(getContext(), "Ошибка изменения имени пользователя", Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.getImageUploadSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(getContext(), "Изображение профиля обновлено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Не удалось загрузить изображение", Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUserData(UserProfileResponse userProfile) {
        tvUsername.setText(userProfile.getUsername());

        String imageUrl = userProfile.getProfileImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_profile_24)
                    .into(ivProfilePicture);
        } else {
            ivProfilePicture.setImageResource(R.drawable.ic_profile_24);
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }


    private void registerImagePickerCallback() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().getContentResolver(),
                                    result.getData().getData()
                            );
                            ivProfilePicture.setImageBitmap(bitmap);
                            profileViewModel.uploadProfileImage(bitmap);

                        } catch (IOException e) {
                            Log.e(TAG, "Error selecting image: " + e.getMessage());
                            Toast.makeText(requireContext(), "Ошибка при выборе изображения", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void handleChangePassword() {
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();

        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        profileViewModel.changePassword(currentPassword, newPassword);
    }

    private void handleChangeUsername() {
        String newUsername = etNewUsername.getText().toString();
        if (newUsername.isEmpty()) {
            Toast.makeText(getContext(), "Введите новое имя пользователя", Toast.LENGTH_SHORT).show();
            return;
        }
        profileViewModel.changeUsername(newUsername);
    }

    private void logoutUser() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("authToken");
        editor.remove("isAdmin");
        editor.apply();

        Intent stopServiceIntent = new Intent(getActivity(), MusicService.class);
        stopServiceIntent.setAction("STOP_SERVICE");
        requireActivity().startService(stopServiceIntent);

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
