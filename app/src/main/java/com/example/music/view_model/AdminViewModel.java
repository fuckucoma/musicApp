package com.example.music.view_model;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.music.models.Track;
import com.example.music.repository.AdminRepository;

import java.util.List;

public class AdminViewModel extends ViewModel {

    private MutableLiveData<List<Track>> tracksLiveData = new MutableLiveData<>();
    private AdminRepository adminRepository = AdminRepository.getInstance();

    public LiveData<List<Track>> getTracksLiveData() {
        return tracksLiveData;
    }

    public void fetchTracks() {
        adminRepository.fetchTracks(new AdminRepository.MyCallback<List<Track>>() {
            @Override
            public void onSuccess(List<Track> data) {
                tracksLiveData.postValue(data);
            }

            @Override
            public void onError(Throwable t) {
                tracksLiveData.postValue(null);
            }
        });
    }

    public void deleteTracks(int trackId) {
        adminRepository.deleteTrack(trackId, new AdminRepository.MyCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                fetchTracks();
            }

            @Override
            public void onError(Throwable t) {
                Log.e("TrackManagementFragment", "Ошибка удаления трека: " + t.getMessage());
            }
        });
    }
}

