package com.example.music;

import android.app.Application;
import android.support.annotation.NonNull;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.music.models.Track;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;

public class PlayerViewModel extends AndroidViewModel {

    // Плеер для SearchFragment и LibraryFragment
    private ExoPlayer player;
    private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private MutableLiveData<Track> currentTrack = new MutableLiveData<>();
    private MutableLiveData<Boolean> isPlayerReady = new MutableLiveData<>(false);

    // Плеер для HomeFragment
    private ExoPlayer homePlayer;
    private boolean isHomePlaying = false;

    private Track currentHomeTrack;

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        // Инициализация плееров
        player = new ExoPlayer.Builder(application.getApplicationContext()).build();
        homePlayer = new ExoPlayer.Builder(application.getApplicationContext()).build();

        initializePlayerListeners();
    }

    public void initializePlayerListeners() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    isPlayerReady.setValue(true);
                    isPlaying.setValue(player.isPlaying());
                } else if (playbackState == Player.STATE_ENDED) {
                    isPlaying.setValue(false);
                }
            }
        });
    }


    // Методы для управления основным плеером
    public void playTrack(String url, Track track) {
        // Останавливаем плеер HomeFragment
        stopHomePlayer();

        player.setMediaItem(MediaItem.fromUri(url));
        player.prepare();
        player.play();
        isPlaying.setValue(true);
        currentTrack.setValue(track);

        currentHomeTrack = track;
    }

    public void pauseTrack() {
        if (player.isPlaying()) {
            player.pause();
            isPlaying.setValue(false);
        }
    }

    public Track getCurrentHomeTrack() {
        return currentHomeTrack;
    }

    public void stopTrack() {
        if (player != null) {
            player.stop();
            isPlaying.setValue(false);
            // Не очищаем currentTrack, чтобы медиабар мог отображать информацию
        }
    }

    // Методы для управления плеером HomeFragment
    public void playHomeTrack(String url, Track track) {
        // Ставим на паузу основной плеер, если он играет
        if (isPlaying.getValue() != null && isPlaying.getValue()) {
            pauseTrack();
        }

        homePlayer.setMediaItem(MediaItem.fromUri(url));
        homePlayer.prepare();
        homePlayer.play();
        isHomePlaying = true;
    }

    public LiveData<Boolean> isPlayerReady() {
        return isPlayerReady;
    }

    public ExoPlayer getPlayerInstance() {
        return player;
    }

    public void resumeTrack() {
        if (player != null) {
            player.play();
            isPlaying.setValue(true);
        }
    }

    public void releasePlayers() {
        if (player != null) {
            player.release();
        }
        if (homePlayer != null) {
            homePlayer.release();
        }
    }

    public void pauseHomeTrack() {
        if (homePlayer.isPlaying()) {
            homePlayer.pause();
            isHomePlaying = false;
        }
    }

    public void stopHomePlayer() {
        if (homePlayer != null) {
            homePlayer.stop();
            isHomePlaying = false;
        }
    }

    // Геттеры
    public LiveData<Boolean> isPlaying() {
        return isPlaying;
    }

    public LiveData<Track> getCurrentTrack() {
        return currentTrack;
    }

    public boolean isHomePlaying() {
        return isHomePlaying;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (player != null) {
            player.release();
        }
        if (homePlayer != null) {
            homePlayer.release();
        }
    }
}
