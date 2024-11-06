package com.example.music;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

public class PlayerViewModel extends ViewModel {
    private ExoPlayer player;
    private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);

    public void setPlayer(ExoPlayer player) {
        this.player = player;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public void playTrack(String url) {
        if (player.isPlaying()) {
            player.stop();
        }
        player.setMediaItem(MediaItem.fromUri(url));
        player.prepare();
        player.play();
        isPlaying.setValue(true);
    }

    public void pauseTrack() {
        if (player.isPlaying()) {
            player.pause();
            isPlaying.setValue(false);
        }
    }

    public LiveData<Boolean> isPlaying() {
        return isPlaying;
    }
}
