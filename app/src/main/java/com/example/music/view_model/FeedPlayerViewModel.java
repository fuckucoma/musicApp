package com.example.music.view_model;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.music.models.Track;
import com.example.music.repository.TrackRepository;
import com.example.test.BuildConfig;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;

public class FeedPlayerViewModel extends AndroidViewModel {
    private TrackRepository trackRepository;
    private ExoPlayer feedPlayer;

    private MutableLiveData<Boolean> feedIsPlaying = new MutableLiveData<>(false);
    private MutableLiveData<Track> feedCurrentTrack = new MutableLiveData<>();

    private Handler feedSeekBarHandler = new Handler(Looper.getMainLooper());
    private MutableLiveData<Long> feedDuration = new MutableLiveData<>(0L);
    private MutableLiveData<Long> feedCurrentPosition = new MutableLiveData<>(0L);

    public FeedPlayerViewModel(@NonNull Application application) {
        super(application);
        trackRepository = TrackRepository.getInstance();
        feedPlayer = new ExoPlayer.Builder(application.getApplicationContext()).build();
        initializeFeedPlayerListeners();
        initializePlaybackStateObserver();
    }

    private void initializeFeedPlayerListeners() {
        feedPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    feedIsPlaying.postValue(feedPlayer.isPlaying());
                    feedDuration.postValue(feedPlayer.getDuration());
                    updateFeedSeekBar();
                } else if (playbackState == Player.STATE_ENDED) {
                    feedIsPlaying.postValue(false);
                    stopFeedSeekBarUpdates();
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlayingNow) {
                feedIsPlaying.postValue(isPlayingNow);
            }

            @Override
            public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition,
                                                @NonNull Player.PositionInfo newPosition,
                                                int reason) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    feedCurrentPosition.postValue(feedPlayer.getCurrentPosition());
                }
            }
        });
    }

    private final Observer<Boolean> isPlayingObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean isPlaying) {
            Log.d("Feed", "isPlaying изменилось: " + isPlaying);
            if (isPlaying != null && isPlaying) {
                if (feedPlayer != null && feedPlayer.isPlaying()) {
                    Log.d("Feed", "MusicService начал воспроизведение. Ставлю FeedPlayer на паузу.");
                    pauseFeedTrack();
                }
            }
        }
    };

    private void initializePlaybackStateObserver() {
        // Подписываемся на изменения isPlaying из TrackRepository
        trackRepository.isPlaying().observeForever(isPlayingObserver);
    }

    public LiveData<Boolean> isFeedPlaying() {
        return feedIsPlaying;
    }

    public LiveData<Track> getFeedCurrentTrack() {
        return feedCurrentTrack;
    }

    public LiveData<Long> getFeedDuration() {
        return feedDuration;
    }

    public LiveData<Long> getFeedCurrentPosition() {
        return feedCurrentPosition;
    }

    public void playFeedTrack(Track track) {
        if (track == null) return;
        String url = generateStreamUrl(track.getId());
        feedPlayer.setMediaItem(MediaItem.fromUri(url));
        feedPlayer.prepare();
        feedPlayer.play();

        feedIsPlaying.postValue(true);
        feedCurrentTrack.setValue(track);
    }

    public void pauseFeedTrack() {
        if (feedPlayer.isPlaying()) {
            feedPlayer.pause();
            feedIsPlaying.postValue(false);
        }
    }

    private String generateStreamUrl(int trackId) {
        return BuildConfig.BASE_URL + "/tracks/" + trackId + "/stream";
    }

    private void updateFeedSeekBar() {
        feedSeekBarHandler.postDelayed(() -> {
            if (feedPlayer.isPlaying()) {
                feedCurrentPosition.postValue(feedPlayer.getCurrentPosition());
                updateFeedSeekBar();
            }
        }, 1000);
    }

    private void stopFeedSeekBarUpdates() {
        feedSeekBarHandler.removeCallbacksAndMessages(null);
    }

    public boolean isHomePlaying() {
        Boolean playing = feedIsPlaying.getValue();
        return playing != null && playing;
    }

    public Track getCurrentHomeTrack() {
        return feedCurrentTrack.getValue();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (feedPlayer != null) {
            feedPlayer.release();
        }
        stopFeedSeekBarUpdates();
    }
}
