package com.example.music.fragments;

// PlayerFragment.java
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.music.PlayerViewModel;
import com.example.music.activity.MainActivity;
import com.example.music.models.Track;
import com.example.music.repository.FavoriteRepository;
import com.example.test.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

public class PlayerFragment extends Fragment {

    private ImageView albumArt;
    private TextView trackTitle, name_artist, current_duration, song_max_duration;
    private SeekBar seekBar;
    private FloatingActionButton playPauseButton;
    private ImageButton btn_favorite;
    private ExtendedFloatingActionButton btnSkipPrevious;
    private ExtendedFloatingActionButton btnSkipNext;

    private PlayerViewModel playerViewModel;
    private FavoriteRepository favoriteRepository;

    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            Long currentPosition = playerViewModel.getCurrentPosition().getValue();
            if (currentPosition != null) {
                seekBar.setProgress(currentPosition.intValue());
            }
            if (playerViewModel.isPlaying().getValue() != null && playerViewModel.isPlaying().getValue()) {
                handler.postDelayed(this, 1000); // Обновляем каждые 1000 мс
            }
        }
    };

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_media_player_view, container, false);

        albumArt = view.findViewById(R.id.player_track_image);
        trackTitle = view.findViewById(R.id.text_view_song_title);
        seekBar = view.findViewById(R.id.seek_bar_main);
        playPauseButton = view.findViewById(R.id.btn_play_pause);
        btn_favorite = view.findViewById(R.id.btn_favorite);
        name_artist = view.findViewById(R.id.name_artist);
        btnSkipPrevious = view.findViewById(R.id.btn_skip_previous);
        btnSkipNext = view.findViewById(R.id.btn_skip_next);
        current_duration = view.findViewById(R.id.current_duration); // NEW
        song_max_duration = view.findViewById(R.id.song_max_duration); // NEW

        // Получаем PlayerViewModel (основной плеер)
        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        // Получаем FavoriteRepository, например, через MainActivity
        if (requireActivity() instanceof MainActivity) {
            favoriteRepository = ((MainActivity) requireActivity()).getFavoriteRepository();
        } else {
            // Либо напрямую
            favoriteRepository = FavoriteRepository.getInstance(requireContext());
        }

        setupPlayerControls();

        return view;
    }

    private void setupPlayerControls() {
        // Наблюдаем за изменениями текущего трека
        playerViewModel.getCurrentTrack().observe(getViewLifecycleOwner(), track -> {
            if (track != null) {
                trackTitle.setText(track.getTitle());
                name_artist.setText(track.getArtist());
                Picasso.get().load(track.getImageUrl()).into(albumArt);

                // Обновляем SeekBar (максимальное значение)
                Long duration = playerViewModel.getDuration().getValue();
                if (duration != null) {
                    seekBar.setMax(duration.intValue());
                    song_max_duration.setText(formatTime(duration));
                }

                Long position = playerViewModel.getCurrentPosition().getValue();
                if (position != null) {
                    seekBar.setProgress(position.intValue());
                    current_duration.setText(formatTime(position));
                }

                // Обновляем иконку избранного
                updateFavoriteButton(track);
            } else {
                Log.e("PlayerFragment", "currentTrack == null");
                trackTitle.setText("");
                name_artist.setText("");
                albumArt.setImageResource(R.drawable.placeholder_image);
                seekBar.setMax(0);
                seekBar.setProgress(0);
                current_duration.setText("00:00"); // NEW
                song_max_duration.setText("00:00"); // NEW
                btn_favorite.setImageResource(R.drawable.ic_favorite_24px);
            }
        });

        // Наблюдаем за состоянием воспроизведения
        playerViewModel.isPlaying().observe(getViewLifecycleOwner(), isPlaying -> {
            playPauseButton.setImageResource(isPlaying ? R.drawable.ic_pause_24px : R.drawable.ic_play_arrow_24px);
        });

        // Наблюдаем за изменениями избранных треков (список ID)
        favoriteRepository.getFavoriteTrackIds().observe(getViewLifecycleOwner(), ids -> {
            Track currentTrack = playerViewModel.getCurrentTrack().getValue();
            if (currentTrack != null) {
                updateFavoriteButton(currentTrack);
            }
        });

        // Кнопка Play/Pause
        playPauseButton.setOnClickListener(v -> {
            Boolean playing = playerViewModel.isPlaying().getValue();
            if (playing != null && playing) {
                playerViewModel.pauseTrack();
            } else {
                playerViewModel.resumeTrack();
            }
        });

        // Кнопки "Следующий" / "Предыдущий" трек
        btnSkipNext.setOnClickListener(v -> playerViewModel.playNextTrack());
        btnSkipPrevious.setOnClickListener(v -> playerViewModel.playPreviousTrack());

        // Кнопка "Избранное"
        btn_favorite.setOnClickListener(v -> {
            Track currentTrack = playerViewModel.getCurrentTrack().getValue();
            if (currentTrack != null) {
                boolean isFavorite = favoriteRepository.isTrackFavorite(currentTrack.getId());
                if (isFavorite) {
                    favoriteRepository.removeTrackFromFavorites(currentTrack);
                } else {
                    favoriteRepository.addTrackToFavorites(currentTrack);
                }
            }
        });

        playerViewModel.getDuration().observe(getViewLifecycleOwner(), duration -> {
            if (duration != null) {
                seekBar.setMax(duration.intValue());
                song_max_duration.setText(formatTime(duration)); // NEW
            }
        });

        // Наблюдаем за изменениями текущей позиции
        playerViewModel.getCurrentPosition().observe(getViewLifecycleOwner(), position -> {
            if (position != null) {
                seekBar.setProgress(position.intValue());
                current_duration.setText(formatTime(position)); // NEW
            }
        });

        // Настройка SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    playerViewModel.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBarRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.postDelayed(updateSeekBarRunnable, 1000);
            }
        });
    }

    // Метод обновления иконки "Избранное"
    private void updateFavoriteButton(Track track) {
        boolean isFavorite = favoriteRepository.isTrackFavorite(track.getId());
        btn_favorite.setImageResource(isFavorite ? R.drawable.ic_heart__24 : R.drawable.ic_favorite_24px);
    }

    private void updateSeekBar() {
        handler.postDelayed(updateSeekBarRunnable, 1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateSeekBarRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();

        Track currentTrack = playerViewModel.getCurrentTrack().getValue();
        if (currentTrack != null) {
            trackTitle.setText(currentTrack.getTitle());
            name_artist.setText(currentTrack.getArtist());
            Picasso.get().load(currentTrack.getImageUrl()).into(albumArt);

            Long duration = playerViewModel.getDuration().getValue();
            if (duration != null) {
                seekBar.setMax(duration.intValue());
               song_max_duration.setText(formatTime(duration));
            }
            Long position = playerViewModel.getCurrentPosition().getValue();
            if (position != null) {
                seekBar.setProgress(position.intValue());
                current_duration.setText(formatTime(duration));
            }
            updateFavoriteButton(currentTrack);
        }
        updateSeekBar();
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(updateSeekBarRunnable);
    }

    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60 ;
        int minutes = (int) ((millis / (1000*60)) % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }
}
