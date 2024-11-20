package com.example.music.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.music.models.Track;

import java.util.ArrayList;
import java.util.List;

public class TrackRepository {

    private List<Track> trackQueue = new ArrayList<>();
    private Track currentTrack;
    private int currentTrackIndex = -1;

    private final MutableLiveData<List<Track>> trackQueueLiveData = new MutableLiveData<>();
    private final MutableLiveData<Track> currentTrackLiveData = new MutableLiveData<>();

    public TrackRepository() {
        trackQueueLiveData.setValue(trackQueue);
        currentTrackLiveData.setValue(currentTrack);
    }

    // Добавить треки в очередь
    public void setTrackQueue(List<Track> tracks) {
        this.trackQueue.clear();
        this.trackQueue.addAll(tracks);
        trackQueueLiveData.setValue(trackQueue);

        if (!tracks.isEmpty() && currentTrack == null) {
            setCurrentTrack(tracks.get(0));
        }
    }

    // Получить очередь треков
    public LiveData<List<Track>> getTrackQueue() {
        return trackQueueLiveData;
    }

    // Установить текущий трек
    public void setCurrentTrack(Track track) {
        this.currentTrack = track;
        this.currentTrackIndex = trackQueue.indexOf(track);
        currentTrackLiveData.setValue(track);
    }

    // Получить текущий трек
    public LiveData<Track> getCurrentTrack() {
        return currentTrackLiveData;
    }

    // Воспроизведение следующего трека
    public Track playNextTrack() {
        if (currentTrackIndex < trackQueue.size() - 1) {
            currentTrackIndex++;
            setCurrentTrack(trackQueue.get(currentTrackIndex));
        }
        return currentTrack;
    }

    // Воспроизведение предыдущего трека
    public Track playPreviousTrack() {
        if (currentTrackIndex > 0) {
            currentTrackIndex--;
            setCurrentTrack(trackQueue.get(currentTrackIndex));
        }
        return currentTrack;
    }
}

