package com.example.music.view_model;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.music.Event;
import com.example.music.response.UserProfileResponse;
import com.example.music.repository.ProfileRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewModel extends AndroidViewModel {

    private final ProfileRepository profileRepository;

    private final LiveData<UserProfileResponse> userProfile;
    private final LiveData<Event<Boolean>> passwordChangeSuccess;
    private final LiveData<Event<Boolean>> usernameChangeSuccess;
    private final LiveData<Event<Boolean>> imageUploadSuccess;
    private final LiveData<Event<String>> errorMessage;
    private final MutableLiveData<UserProfileResponse> userProfileByIdLiveData = new MutableLiveData<>();


    public ProfileViewModel(@NonNull Application application) {
        super(application);
        profileRepository = ProfileRepository.getInstance();
        userProfile = profileRepository.getUserProfileLiveData();
        passwordChangeSuccess = profileRepository.getPasswordChangeSuccess();
        usernameChangeSuccess = profileRepository.getUsernameChangeSuccess();
        imageUploadSuccess = profileRepository.getImageUploadSuccess();
        errorMessage = profileRepository.getErrorMessageLiveData();
    }

    public LiveData<UserProfileResponse> getUserByIdLiveData() {
        return userProfileByIdLiveData;
    }

    public void fetchUserById(int userId) {
        profileRepository.getUserById(userId);
    }

    public void updateUserProfileById(UserProfileResponse userProfileResponse) {
        userProfileByIdLiveData.postValue(userProfileResponse);
    }

    public LiveData<UserProfileResponse> getUserProfile() {
        return userProfile;
    }

    public LiveData<Event<Boolean>> getPasswordChangeSuccess() {
        return passwordChangeSuccess;
    }

    public LiveData<Event<Boolean>> getUsernameChangeSuccess() {
        return usernameChangeSuccess;
    }

    public LiveData<Event<Boolean>> getImageUploadSuccess() {
        return imageUploadSuccess;
    }

    public LiveData<Event<String>> getErrorMessage() {
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
