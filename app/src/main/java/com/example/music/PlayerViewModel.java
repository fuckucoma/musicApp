package com.example.music;

import android.app.Application;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.music.models.Track;
import com.example.test.BuildConfig;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerViewModel extends AndroidViewModel {

    // Плеер для SearchFragment и LibraryFragment
    private ExoPlayer player;
    private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private MutableLiveData<Track> currentTrack = new MutableLiveData<>();
    private MutableLiveData<Boolean> isPlayerReady = new MutableLiveData<>(false);

    private MutableLiveData<Long> currentPosition = new MutableLiveData<>(0L);
    private MutableLiveData<Long> duration = new MutableLiveData<>(0L);

    private List<Track> trackList = new ArrayList<>(); // Список треков
    private int currentTrackIndex = -1; // Индекс текущего трека

    private Handler seekBarHandler = new Handler();

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
                    duration.setValue(player.getDuration());
                    updateSeekBar(); // Запускаем обновление
                } else if (playbackState == Player.STATE_ENDED) {
                    isPlaying.setValue(false);
                    stopSeekBarUpdates(); // Останавливаем обновление
                }
            }
        });

        player.addListener(new Player.Listener() {
            @Override
            public void onPositionDiscontinuity(int reason) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    currentPosition.setValue(player.getCurrentPosition());
                }
            }
        });


        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING) {
                    updateSeekBar();
                }
            }
        });

    }

    public void setTrackList(List<Track> tracks) {
        this.trackList.clear();
        this.trackList.addAll(tracks);
        currentTrackIndex = !tracks.isEmpty() ? 0 : -1; // Устанавливаем индекс первого трека
    }

    public LiveData<Long> getCurrentPosition() {
        return currentPosition;
    }

    public LiveData<Long> getDuration() {
        return duration;
    }

    public void playNextTrack() {
        if (currentTrackIndex >= 0 && currentTrackIndex < trackList.size() - 1) {
            currentTrackIndex++;
            Track nextTrack = trackList.get(currentTrackIndex);
            String url = generateStreamUrl(nextTrack.getId());
            playTrack(url, nextTrack);
        } else {
            Log.w("PlayerViewModel", "Следующий трек недоступен");
        }
    }

    public void playPreviousTrack() {
        if (currentTrackIndex > 0) {
            currentTrackIndex--;
            Track previousTrack = trackList.get(currentTrackIndex);
            String url = generateStreamUrl(previousTrack.getId());
            playTrack(url, previousTrack);
        } else {
            Log.w("PlayerViewModel", "Предыдущий трек недоступен");
        }
    }

    // Методы для управления основным плеером
    public void playTrack(String url, Track track) {
        if (track == null) return;

        // Останавливаем плеер HomeFragment
        stopHomePlayer();

        url = generateStreamUrl(track.getId());

        player.setMediaItem(MediaItem.fromUri(url));
        player.prepare();
        player.play();

        isPlaying.setValue(true);
        currentTrack.setValue(track);

        int index = trackList.indexOf(track);
        if (index != -1) {
            currentTrackIndex = index;
        } else {
            // Если трек не найден, логируем проблему
            currentTrackIndex = -1;
            Log.e("PlayerViewModel", "Трек не найден в trackList");
        }

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
        stopSeekBarUpdates(); // Останавливаем обновление SeekBar
    }

    private String generateStreamUrl(int trackId) {
        return BuildConfig.BASE_URL+"/tracks/" + trackId + "/stream";
    }

    public void resetSeekBar() {
        currentPosition.postValue(0L);
        duration.postValue(0L);
    }

    private void updateSeekBar() {
        seekBarHandler.postDelayed(() -> {
            if (player.isPlaying()) {
                currentPosition.postValue(player.getCurrentPosition());
                updateSeekBar();
            }
        }, 1000); // Обновляем каждые 1000 мс
    }

    private void stopSeekBarUpdates() {
        seekBarHandler.removeCallbacksAndMessages(null);
    }
}
