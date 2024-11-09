package com.example.music;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.music.models.Track;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

public class PlayerViewModel extends ViewModel {
    private ExoPlayer player;
    private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private MutableLiveData<Track> currentTrack = new MutableLiveData<>();
    private boolean isFromHomeFragment = false; // Флаг для определения источника трека

    public void setPlayer(ExoPlayer player) {
        this.player = player;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public void playTrack(String url, Track track, boolean fromHomeFragment) {
        player.setMediaItem(MediaItem.fromUri(url));
        player.prepare();
        player.play();
        isPlaying.setValue(true);
        currentTrack.setValue(track);
        isFromHomeFragment = fromHomeFragment;
    }

    public void pauseTrack() {
        if (player != null && player.isPlaying()) {
            player.pause();
            isPlaying.setValue(false);
        }
    }

    public void stopTrack() {
        if (player != null) {
            player.stop();
            isPlaying.setValue(false);
            currentTrack.setValue(null);
            isFromHomeFragment = false; // Сбрасываем флаг
        }
    }

    public LiveData<Boolean> isPlaying() {
        return isPlaying;
    }

    public LiveData<Track> getCurrentTrack() {
        return currentTrack;
    }

    public boolean isFromHomeFragment() {
        return isFromHomeFragment;
    }
}
