package com.example.music;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.music.models.Track;
import com.example.music.repository.TrackRepository;
import com.example.test.BuildConfig;
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

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        trackRepository = TrackRepository.getInstance();
        currentTrack = trackRepository.getCurrentTrack();
        isPlaying = trackRepository.isPlaying();
        isPlayerReady = trackRepository.isPlayerReady();
        duration = trackRepository.getDuration();
        currentPosition = trackRepository.getCurrentPosition();
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
