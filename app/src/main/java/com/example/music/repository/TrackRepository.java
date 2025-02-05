package com.example.music.repository;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.music.MyApp;
import com.example.music.PlaybackSource;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.Track;
import com.example.music.response.FavoriteResponse;
import com.example.music.service.MusicService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackRepository {
    private static TrackRepository instance;
    private ApiService apiService;

    private MutableLiveData<List<Track>> feedTracksLiveData = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<Track>> searchTracksLiveData = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<Track>> libraryTracksLiveData = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Track> currentTrackLiveData = new MutableLiveData<>();

    private MutableLiveData<PlaybackSource> currentPlaybackSourceLiveData = new MutableLiveData<>(null);

    private MutableLiveData<Boolean> isPlayingLiveData = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isPlayerReadyLiveData = new MutableLiveData<>(false);
    private MutableLiveData<Long> durationLiveData = new MutableLiveData<>(0L);
    private MutableLiveData<Long> currentPositionLiveData = new MutableLiveData<>(0L);

    private MutableLiveData<Boolean> isRepeatEnabled = new MutableLiveData<>(false);



    private TrackRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);

    }

    public static synchronized TrackRepository getInstance() {
        if (instance == null) {
            instance = new TrackRepository();
        }
        return instance;
    }

    public LiveData<Boolean> isRepeatEnabled() {
        return isRepeatEnabled;
    }

    public void setRepeatEnabled(boolean isEnabled) {
        isRepeatEnabled.postValue(isEnabled);
    }

    public LiveData<PlaybackSource> getCurrentPlaybackSource() {
        return currentPlaybackSourceLiveData;
    }

    public void setCurrentPlaybackSource(PlaybackSource source) {
        currentPlaybackSourceLiveData.postValue(source);
    }


    public LiveData<List<Track>> getFeedTracks() { return feedTracksLiveData; }
    public LiveData<List<Track>> getSearchTracks() { return searchTracksLiveData; }
    public LiveData<List<Track>> getLibraryTracks() { return libraryTracksLiveData; }
    public LiveData<Track> getCurrentTrack() { return currentTrackLiveData; }

    public void setCurrentTrack(Track track) {
        Log.d("TrackRepository", "Setting current track to: " + track.getTitle());
        currentTrackLiveData.postValue(track);
    }

    public void loadFeedTracks() {
        apiService.getAllTracks().enqueue(new Callback<List<Track>>() {
            @Override
            public void onResponse(Call<List<Track>> call, Response<List<Track>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    feedTracksLiveData.postValue(response.body());
                    Log.d("TrackRepository", "Feed tracks loaded: " + response.body().size());
                } else {
                    Log.e("TrackRepository", "Ошибка загрузки ленты: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Track>> call, Throwable t) {
                Log.e("TrackRepository", "Ошибка сети при загрузке ленты: " + t.getMessage());
            }
        });
    }

    public void searchTracks(String query) {
        apiService.searchTracks(query).enqueue(new Callback<List<Track>>() {
            @Override
            public void onResponse(Call<List<Track>> call, Response<List<Track>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchTracksLiveData.postValue(response.body());
                    Log.d("TrackRepository", "Search results: " + response.body().size());
                } else {
                    Log.e("TrackRepository", "Ошибка поиска: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Track>> call, Throwable t) {
                Log.e("TrackRepository", "Ошибка сети при поиске: " + t.getMessage());
            }
        });
    }

    public void loadLibraryTracks() {
        apiService.getLibraryTracks().enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Track> tracks = new ArrayList<>();
                    for (FavoriteResponse.FavoriteTrack ft : response.body().getFavoriteTracks()) {
                        Track track = new Track(
                                ft.getTrackId(),
                                ft.getTitle(),
                                ft.getArtist(),
                                ft.getImageUrl(),
                                ft.getFilename(),
                                ft.getCreatedAt()
                        );
                        tracks.add(track);
                    }
                    libraryTracksLiveData.postValue(tracks);

                    Log.d("TrackRepository", "Library tracks loaded: " + tracks.size());
                } else {
                    Log.e("TrackRepository", "Ошибка загрузки библиотеки: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                Log.e("TrackRepository", "Ошибка сети при загрузке библиотеки: " + t.getMessage());
            }
        });
    }

    public void playTrack(Track track, PlaybackSource source) {
        Log.d("TrackRepository", "playTrack called for track: " + track.getTitle() + ", Source: " + source);
        setCurrentPlaybackSource(source);
        setCurrentTrack(track);
        Intent intent = new Intent(MyApp.getInstance(), MusicService.class);
        intent.setAction("PLAY_TRACK");
        intent.putExtra("TRACK_ID", track.getId());
        intent.putExtra("PLAYBACK_SOURCE", source.name());
        MyApp.getInstance().startService(intent);
    }



    public void pauseTrack() {
        Intent intent = new Intent(MyApp.getInstance(), MusicService.class);
        intent.setAction("PAUSE");
        MyApp.getInstance().startService(intent);
    }

    public void resumeTrack() {
        Intent intent = new Intent(MyApp.getInstance(), MusicService.class);
        intent.setAction("RESUME");
        MyApp.getInstance().startService(intent);
    }

    public void playNextTrack() {
        Track currentTrack = getCurrentTrack().getValue();
        PlaybackSource currentSource = getCurrentPlaybackSource().getValue();

        Log.d("TrackRepository", "playNextTrack called. CurrentSource: " + currentSource + ", CurrentTrack: " + (currentTrack != null ? currentTrack.getTitle() : "null"));

        if (currentSource != null && currentTrack != null) {
            Track nextTrack = getNextTrack(currentSource, currentTrack);
            if (nextTrack != null) {
                Log.d("TrackRepository", "Next track found: " + nextTrack.getTitle());
                setCurrentTrack(nextTrack);
                playTrack(nextTrack, currentSource);
            } else {
                Log.d("TrackRepository", "No next track available.");
            }
        }
    }

    public void playPreviousTrack() {
        Track currentTrack = getCurrentTrack().getValue();
        PlaybackSource currentSource = getCurrentPlaybackSource().getValue();

        Log.d("TrackRepository", "playPreviousTrack called. CurrentSource: " + currentSource + ", CurrentTrack: " + (currentTrack != null ? currentTrack.getTitle() : "null"));

        if (currentSource != null && currentTrack != null) {
            Track previousTrack = getPreviousTrack(currentSource, currentTrack);
            if (previousTrack != null) {
                Log.d("TrackRepository", "Previous track found: " + previousTrack.getTitle());
                setCurrentTrack(previousTrack);
                playTrack(previousTrack, currentSource);
            } else {
                Log.d("TrackRepository", "No previous track available.");
            }
        }
    }

    public void updatePlaybackState(boolean isPlaying) {
        isPlayingLiveData.postValue(isPlaying);
    }

    public LiveData<Boolean> isPlaying() {
        return isPlayingLiveData;
    }

    public LiveData<Boolean> isPlayerReady() {
        return isPlayerReadyLiveData;
    }

    public LiveData<Long> getDuration() {
        return durationLiveData;
    }

    public LiveData<Long> getCurrentPosition() {
        return currentPositionLiveData;
    }

    public void updateIsPlayerReady(boolean isPlayerReady) {
        isPlayerReadyLiveData.postValue(isPlayerReady);
    }

    public void updateDuration(long duration) {
        durationLiveData.postValue(duration);
    }

    public void updateCurrentPosition(long position) {
        currentPositionLiveData.postValue(position);
    }

    public void seekTo(long position) {
        Intent intent = new Intent(MyApp.getInstance(), MusicService.class);
        intent.setAction("SEEK_TO");
        intent.putExtra("POSITION", position);
        MyApp.getInstance().startService(intent);
    }

    public Track getNextTrack(PlaybackSource playbackSource, Track currentTrack) {
        List<Track> trackList = getTrackListBySource(playbackSource);
        if (trackList == null || trackList.isEmpty() || currentTrack == null) return null;

        int index = trackList.indexOf(currentTrack);
        if (index == -1 || index >= trackList.size() - 1) {
            return null; // Нет следующего трека
        }
        return trackList.get(index + 1);
    }

    public Track getPreviousTrack(PlaybackSource playbackSource, Track currentTrack) {
        List<Track> trackList = getTrackListBySource(playbackSource);
        if (trackList == null || trackList.isEmpty() || currentTrack == null) return null;

        int index = trackList.indexOf(currentTrack);
        if (index <= 0) {
            return null;
        }
        return trackList.get(index - 1);
    }

    private List<Track> getTrackListBySource(PlaybackSource playbackSource) {
        switch (playbackSource) {
            case SEARCH:
                return searchTracksLiveData.getValue();
            case LIBRARY:
                return libraryTracksLiveData.getValue();
            default:
                return null;
        }
    }

    public Track getTrackById(int trackId) {
        // Реализация поиска трека по ID в списках Search и Library
        List<Track> searchTracks = getSearchTracks().getValue();
        List<Track> libraryTracks = getLibraryTracks().getValue();

        if (searchTracks != null) {
            for (Track track : searchTracks) {
                if (track.getId() == trackId) return track;
            }
        }
        if (libraryTracks != null) {
            for (Track track : libraryTracks) {
                if (track.getId() == trackId) return track;
            }
        }
        return null;
    }
}
