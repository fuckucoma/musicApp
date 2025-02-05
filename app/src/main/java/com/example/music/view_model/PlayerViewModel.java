package com.example.music.view_model;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.music.PlaybackSource;
import com.example.music.models.Track;
import com.example.music.repository.TrackRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PlayerViewModel extends AndroidViewModel {
    private TrackRepository trackRepository;
    private LiveData<Track> currentTrack;
    private LiveData<Boolean> isPlaying;
    private LiveData<Boolean> isPlayerReady;
    private LiveData<Long> duration;
    private LiveData<Long> currentPosition;
    private Handler seekBarHandler = new Handler(Looper.getMainLooper());
    private List<Track> currentTrackList = new ArrayList<>();
    private LiveData<Boolean> isRepeatEnabled;
    private MutableLiveData<List<Track>> listeningHistoryLiveData = new MutableLiveData<>(new ArrayList<>());

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        trackRepository = TrackRepository.getInstance();
        currentTrack = trackRepository.getCurrentTrack();
        isPlaying = trackRepository.isPlaying();
        isPlayerReady = trackRepository.isPlayerReady();
        duration = trackRepository.getDuration();
        currentPosition = trackRepository.getCurrentPosition();
        isRepeatEnabled = trackRepository.isRepeatEnabled();
        listeningHistoryLiveData.postValue(getHistoryTracksPreferences());
        Log.e("History","история :" + listeningHistoryLiveData.getValue().toString());
    }


    public LiveData<Boolean> isRepeatModeEnabled() {
        return isRepeatEnabled;
    }

    public void toggleRepeatMode() {
        boolean currentState = isRepeatEnabled.getValue() != null && isRepeatEnabled.getValue();
        TrackRepository.getInstance().setRepeatEnabled(!currentState);
    }

    public LiveData<Track> getCurrentTrack() {
        return trackRepository.getCurrentTrack();
    }

    public void SetCurrentTrack(Track track){
         trackRepository.setCurrentTrack(track);
    }

    public void seekTo(long position) {
        trackRepository.seekTo(position);
    }

    public LiveData<Boolean> isPlaying() {
        return isPlaying;
    }

    public LiveData<Boolean> isPlayerReady() {
        return isPlayerReady;
    }

    public LiveData<Long> getDuration() {
        return duration;
    }

    public LiveData<Long> getCurrentPosition() {
        return currentPosition;
    }


    public void playTrack(Track track, PlaybackSource source) {
        trackRepository.playTrack(track, source);
        addTrackToHistory(track);
    }

    private void addTrackToHistory(Track track) {
        List<Track> currentHistory = listeningHistoryLiveData.getValue();
        if (currentHistory == null) {
            currentHistory = new ArrayList<>();
        }

        if (!currentHistory.contains(track)) {

            if (currentHistory.size() >= 15) {
                currentHistory.remove(0);
            }
            currentHistory.add(track);
            listeningHistoryLiveData.postValue(currentHistory);
        }

        Gson gson = new Gson();
        String jsonHistory = gson.toJson(currentHistory);

        // Сохраняем строку JSON в SharedPreferences
        SharedPreferences prefs = getApplication().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("track_history", jsonHistory);
        editor.apply();

    }

    public LiveData<List<Track>> getListeningHistory() {
        return listeningHistoryLiveData;
    }

    public List<Track> getHistoryTracksPreferences() {
        SharedPreferences prefs = getApplication().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String jsonHistory = prefs.getString("track_history", "[]");  // по умолчанию возвращаем пустой список

        Gson gson = new Gson();
        Type type = new TypeToken<List<Track>>() {}.getType();  // Тип для списка Track
        return gson.fromJson(jsonHistory, type);  // Десериализуем строку JSON обратно в список
    }

    public void pauseTrack() {
        trackRepository.pauseTrack();
    }

    public void resumeTrack() {
        trackRepository.resumeTrack();
    }

    public void playNextTrack() {
        trackRepository.playNextTrack();
    }

    public void playPreviousTrack() {
        trackRepository.playPreviousTrack();
    }
}
