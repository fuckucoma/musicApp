package com.example.music.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context; // ADF (AudioFocus)
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioFocusRequest; // ADF (AudioFocus)
import android.media.AudioManager;       // ADF (AudioFocus)
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.music.PlaybackSource;
import com.example.music.activity.MainActivity;
import com.example.music.models.Track;
import com.example.music.repository.TrackRepository;
import com.example.test.BuildConfig;
import com.example.test.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicService extends LifecycleService {

    private static final String CHANNEL_ID = "MusicServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private ExoPlayer exoPlayer;
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private Track currentTrack;

    private ExecutorService executorService;
    private Handler positionUpdateHandler;
    private Runnable positionUpdateRunnable;

    // ADF (AudioFocus) : Добавляем поля для управления аудио-фокусом
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;

    // Обработчик изменения аудио-фокуса
    private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                // Полная потеря фокуса: останавливаем воспроизведение
                pauseTrack();
                // Часто при потере фокуса надолго мы освобождаем его совсем.
                abandonAudioFocus();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Временная потеря фокуса (например, звонок) — ставим на паузу
                pauseTrack();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Можно «продолжать, но потише». При желании:
                // exoPlayer.setVolume(0.2f);
                // Но можно и просто сделать паузу.
                pauseTrack();
                break;

            case AudioManager.AUDIOFOCUS_GAIN:
                // Фокус вернулся: восстанавливаем громкость и/или продолжаем воспроизведение
                // exoPlayer.setVolume(1.0f);
                if (!exoPlayer.isPlaying()) {
                    exoPlayer.play();
                }
                break;
        }
    };

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MusicService", "onCreate called");

        exoPlayer = new ExoPlayer.Builder(this).build();
        // Установим атрибуты звука для ExoPlayer. handleAudioFocus=false, т.к. мы сами фокус берём вручную:
        com.google.android.exoplayer2.audio.AudioAttributes audioAttributes =
                new com.google.android.exoplayer2.audio.AudioAttributes.Builder()
                        .setUsage(com.google.android.exoplayer2.C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build();
        exoPlayer.setAudioAttributes(audioAttributes, /* handleAudioFocus= */ false);

        mediaSession = new MediaSessionCompat(this, "MusicService");
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );
        mediaSession.setActive(true);

        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_SEEK_TO // NEW
                );
        mediaSession.setPlaybackState(stateBuilder.build());

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                exoPlayer.seekTo(pos);
                TrackRepository.getInstance().updateCurrentPosition(pos);
                updatePlaybackStateCompat(exoPlayer.isPlaying());
            }
        });

        createNotificationChannel();

        executorService = Executors.newSingleThreadExecutor();

        positionUpdateHandler = new Handler(Looper.getMainLooper());
        positionUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (exoPlayer != null && exoPlayer.isPlaying()) {
                    long currentPos = exoPlayer.getCurrentPosition();
                    TrackRepository.getInstance().updateCurrentPosition(currentPos);
                    // Обновляем состояние плеера (в т.ч. позицию) для отображения seek bar
                    updatePlaybackStateCompat(true);
                    positionUpdateHandler.postDelayed(this, 1000);
                }
            }
        };

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    long duration = exoPlayer.getDuration();
                    if (duration > 0) {
                        TrackRepository.getInstance().updateDuration(duration);
                        TrackRepository.getInstance().updateIsPlayerReady(true);
                        updateMediaSessionMetadata(currentTrack, null);
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    Boolean isRepeatEnabled = TrackRepository.getInstance().isRepeatEnabled().getValue();
                    if (isRepeatEnabled != null && isRepeatEnabled) {
                        exoPlayer.seekTo(0);
                        exoPlayer.play();
                    } else {
                        TrackRepository.getInstance().playNextTrack();
                    }
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                TrackRepository.getInstance().updatePlaybackState(isPlaying);
                if (currentTrack != null) {
                    buildNotification(currentTrack, generateAction(
                            isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                            isPlaying ? "Pause" : "Play",
                            isPlaying ? "PAUSE" : "PLAY"
                    ));
                }
                updatePlaybackStateCompat(isPlaying);
                if (isPlaying) {
                    positionUpdateHandler.removeCallbacks(positionUpdateRunnable);
                    positionUpdateHandler.post(positionUpdateRunnable);
                } else {
                    positionUpdateHandler.removeCallbacks(positionUpdateRunnable);
                    TrackRepository.getInstance().updateCurrentPosition(exoPlayer.getCurrentPosition());
                }
            }
        });

        TrackRepository.getInstance().getCurrentTrack().observe(this, track -> {
            Log.d("MusicService", "Current track changed: " + (track != null ? track.getTitle() : "null"));
            currentTrack = track;
            if (currentTrack != null) {
                playTrack(currentTrack);
            } else {
                pauseTrack();
                buildNotification(null, generateAction(R.drawable.ic_play, "Play", "PLAY"));
            }
        });

        // ADF (AudioFocus): Инициализируем AudioManager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initAudioFocusRequest(); // создадим (или подготовим) audioFocusRequest
    }

    // ADF (AudioFocus): Инициализируем AudioFocusRequest для Android O и выше
    private void initAudioFocusRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                            new android.media.AudioAttributes.Builder()
                                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                    )
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build();
        }
    }

    // ADF (AudioFocus): Запрашиваем аудио-фокус
    private boolean requestAudioFocus() {
        if (audioManager == null) return false;
        int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest == null) return false;
            result = audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            // Для более ранних версий
            result = audioManager.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
            );
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    // ADF (AudioFocus): Освобождаем фокус
    private void abandonAudioFocus() {
        if (audioManager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            }
        } else {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }
    }

    private void updatePlaybackStateCompat(boolean isPlaying) {
        if (exoPlayer == null) return;

        long currentPos = exoPlayer.getCurrentPosition();
        float playbackSpeed = isPlaying ? 1.0f : 0f;
        int state = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;

        stateBuilder.setState(state, currentPos, playbackSpeed);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private void updateMediaSessionMetadata(Track track, @Nullable Bitmap albumArt) {
        if (track == null) return;

        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getArtist());

        long duration = exoPlayer.getDuration();
        if (duration > 0) {
            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);
        }

        if (albumArt != null) {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt);
        } else {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                    BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_image));
        }

        mediaSession.setMetadata(metadataBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MusicService", "onStartCommand called");
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                Log.d("MusicService", "Action received: " + action);
                switch (action) {
                    case "PLAY":
                        exoPlayer.play();
                        break;
                    case "PAUSE":
                        exoPlayer.pause();
                        break;
                    case "RESUME":
                        exoPlayer.play();
                        break;
                    case "NEXT":
                        playNextTrack();
                        break;
                    case "PREVIOUS":
                        playPreviousTrack();
                        break;
                    case "PLAY_TRACK":
                        int trackId = intent.getIntExtra("TRACK_ID", -1);
                        String sourceString = intent.getStringExtra("PLAYBACK_SOURCE");
                        if (trackId != -1 && sourceString != null) {
                            PlaybackSource source = PlaybackSource.valueOf(sourceString);
                            Track track = TrackRepository.getInstance().getTrackById(trackId);
                            if (track != null) {
                                TrackRepository.getInstance().setCurrentPlaybackSource(source);
                                TrackRepository.getInstance().setCurrentTrack(track);
                            } else {
                                Log.e("MusicService", "Track not found with ID: " + trackId);
                            }
                        }
                        break;
                    case "SEEK_TO":
                        long position = intent.getLongExtra("POSITION", 0L);
                        exoPlayer.seekTo(position);
                        TrackRepository.getInstance().updateCurrentPosition(position);
                        updatePlaybackStateCompat(exoPlayer.isPlaying());
                        break;
                    case "STOP_SERVICE":
                        stopMusicService();
                        break;
                    default:
                        break;
                }
            } else if (intent.hasExtra("TRACK_ID")) {
                int trackId = intent.getIntExtra("TRACK_ID", -1);
                String sourceString = intent.getStringExtra("PLAYBACK_SOURCE");
                Log.d("MusicService", "Track ID received (no action): " + trackId);
                if (trackId != -1 && sourceString != null) {
                    PlaybackSource source = PlaybackSource.valueOf(sourceString);
                    Track track = TrackRepository.getInstance().getTrackById(trackId);
                    if (track != null) {
                        TrackRepository.getInstance().setCurrentPlaybackSource(source);
                        TrackRepository.getInstance().setCurrentTrack(track);
                    } else {
                        Log.e("MusicService", "Track not found by ID: " + trackId);
                    }
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void playTrack(Track track) {
        // ADF (AudioFocus) : Запрашиваем фокус перед воспроизведением
        if (!requestAudioFocus()) {
            // Если фокус не дали, не начинаем воспроизведение
            Log.d("MusicService", "AudioFocus NOT granted, skipping play.");
            return;
        }

        String trackUrl = generateStreamUrl(track.getId());
        Log.d("MusicService", "Playing track: " + track.getTitle() + " URL: " + trackUrl);
        Log.d("MusicService", "Track image URL: " + track.getImageUrl());

        MediaItem mediaItem = MediaItem.fromUri(trackUrl);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();

        buildNotification(track, generateAction(R.drawable.ic_pause, "Pause", "PAUSE"));
    }

    private void pauseTrack() {
        if (exoPlayer.isPlaying()) {
            exoPlayer.pause();
        }
    }

    private void handlePlayPause() {
        if (exoPlayer.isPlaying()) {
            exoPlayer.pause();
        } else {
            // ADF (AudioFocus) : При нажатии Play — опять нужно убедиться, что у нас есть фокус
            if (requestAudioFocus()) {
                exoPlayer.play();
            }
        }
    }

    private void playNextTrack() {
        TrackRepository.getInstance().playNextTrack();
    }

    private void playPreviousTrack() {
        TrackRepository.getInstance().playPreviousTrack();
    }

    @SuppressLint("ForegroundServiceType")
    private void buildNotification(Track track, NotificationCompat.Action action) {
        if (track == null) {
            Log.d("MusicService", "buildNotification: track is null");
            return;
        }

        Log.d("MusicService", "buildNotification: Building notification for track: " + track.getTitle());

        int largeIconSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                128,
                getResources().getDisplayMetrics()
        );

        Glide.with(this)
                .asBitmap()
                .load(track.getImageUrl())
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .override(largeIconSize, largeIconSize)
                        .centerCrop()
                        .format(DecodeFormat.PREFER_ARGB_8888))
                .into(new CustomTarget<Bitmap>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        boolean isPlaying = exoPlayer.isPlaying();

                        updateMediaSessionMetadata(track, resource);

                        Intent notificationIntent = new Intent(MusicService.this, MainActivity.class);
                        notificationIntent.setAction("ACTION_OPEN_PLAYER");
                        notificationIntent.putExtra("TRACK_ID", track.getId());
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                                MusicService.this,
                                0,
                                notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        );

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MusicService.this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.icon)
                                .setContentTitle(track.getTitle())
                                .setContentText(track.getArtist())
                                .setLargeIcon(resource)
                                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                        .setMediaSession(mediaSession.getSessionToken())
                                        .setShowActionsInCompactView(1))
                                .addAction(generateAction(R.drawable.ic_skip_previous_24px, "Previous", "PREVIOUS"))
                                .addAction(action)
                                .addAction(generateAction(R.drawable.ic_skip_next_24px, "Next", "NEXT"))
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setContentIntent(contentPendingIntent)
                                .setAutoCancel(false);

                        Notification notification = builder.build();
                        startForeground(NOTIFICATION_ID, notification);

                        updatePlaybackStateCompat(isPlaying);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) { }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Bitmap fallback = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_image);
                        updateMediaSessionMetadata(track, fallback);

                        Intent notificationIntent = new Intent(MusicService.this, MainActivity.class);
                        notificationIntent.setAction("ACTION_OPEN_PLAYER");
                        notificationIntent.putExtra("TRACK_ID", track.getId());
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                                MusicService.this,
                                0,
                                notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        );

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MusicService.this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.icon)
                                .setContentTitle(track.getTitle())
                                .setContentText(track.getArtist())
                                .setLargeIcon(fallback)
                                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                        .setMediaSession(mediaSession.getSessionToken())
                                        .setShowActionsInCompactView(1))
                                .addAction(generateAction(R.drawable.ic_skip_previous_24px, "Previous", "PREVIOUS"))
                                .addAction(action)
                                .addAction(generateAction(R.drawable.ic_skip_next_24px, "Next", "NEXT"))
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setContentIntent(contentPendingIntent)
                                .setAutoCancel(false);

                        Notification notification = builder.build();
                        startForeground(NOTIFICATION_ID, notification);

                        updatePlaybackStateCompat(exoPlayer.isPlaying());
                    }
                });
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private String generateStreamUrl(int trackId) {
        return BuildConfig.BASE_URL + "/tracks/" + trackId + "/stream";
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Music Service Channel";
            String description = "Channel for music service notifications";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MusicService", "onDestroy called");
        if (exoPlayer != null) {
            exoPlayer.release();
        }
        if (mediaSession != null) {
            mediaSession.release();
        }
        stopForeground(true);
        if (executorService != null) {
            executorService.shutdown();
        }
        if (positionUpdateHandler != null && positionUpdateRunnable != null) {
            positionUpdateHandler.removeCallbacks(positionUpdateRunnable);
        }
        // ADF (AudioFocus) : Освобождаем фокус в onDestroy()
        abandonAudioFocus();
    }

    public void stopMusicService() {
        stopForeground(true);
        stopSelf();
        if (exoPlayer != null) {
            exoPlayer.release();
        }
        if (mediaSession != null) {
            mediaSession.release();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
        if (positionUpdateHandler != null && positionUpdateRunnable != null) {
            positionUpdateHandler.removeCallbacks(positionUpdateRunnable);
        }
        // ADF (AudioFocus) : Освобождаем фокус
        abandonAudioFocus();
    }
}

