package com.example.music.view_model;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.music.response.UserProfileResponse;
import com.example.music.repository.ProfileRepository;

public class ProfileViewModel extends AndroidViewModel {

    private final ProfileRepository profileRepository;

    private final LiveData<UserProfileResponse> userProfile;
    private final LiveData<Boolean> passwordChangeSuccess;
    private final LiveData<Boolean> usernameChangeSuccess;
    private final LiveData<Boolean> imageUploadSuccess;
    private final LiveData<String> errorMessage;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        profileRepository = ProfileRepository.getInstance();

        userProfile = profileRepository.getUserProfileLiveData();
        passwordChangeSuccess = profileRepository.getPasswordChangeSuccess();
        usernameChangeSuccess = profileRepository.getUsernameChangeSuccess();
        imageUploadSuccess = profileRepository.getImageUploadSuccess();
        errorMessage = profileRepository.getErrorMessageLiveData();
    }

    public LiveData<UserProfileResponse> getUserProfile() {
        return userProfile;
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

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchUserProfile() {
        profileRepository.fetchUserProfile();
    }

    public void changePassword(String currentPassword, String newPassword) {
        profileRepository.changePassword(currentPassword, newPassword);
    }

    public void changeUsername(String newUsername) {
        profileRepository.changeUsername(newUsername);
    }

    public void uploadProfileImage(Bitmap bitmap) {
        profileRepository.uploadProfileImage(bitmap);
    }

}
