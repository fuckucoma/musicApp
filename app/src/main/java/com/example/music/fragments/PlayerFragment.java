package com.example.music.fragments;

// PlayerFragment.java
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.music.models.Track;
import com.example.test.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

public class PlayerFragment extends Fragment {

    private ImageView albumArt;
    private TextView trackTitle,name_artist;
    private SeekBar seekBar;
    private FloatingActionButton playPauseButton;
    private ImageButton btn_favorite;
    private ExtendedFloatingActionButton btnSkipPrevious;
    private ExtendedFloatingActionButton btnSkipNext;

    private PlayerViewModel playerViewModel;
    private Handler handler = new Handler();

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_media_player_view, container, false);

        albumArt = view.findViewById(R.id.player_track_image);
        trackTitle = view.findViewById(R.id.text_view_song_title);
        seekBar = view.findViewById(R.id.seek_bar_main);
        playPauseButton = view.findViewById(R.id.btn_play_pause);
        btn_favorite = view.findViewById(R.id.btn_favorite);
        name_artist = view.findViewById(R.id.name_artist);
        btnSkipPrevious = view.findViewById(R.id.btn_skip_previous);
        btnSkipNext = view.findViewById(R.id.btn_skip_next);

        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);

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

                playerViewModel.getDuration().observe(getViewLifecycleOwner(), duration -> {
                    if (duration != null) {
                        seekBar.setMax(duration.intValue());
                    }
                });

                playerViewModel.getCurrentPosition().observe(getViewLifecycleOwner(), position -> {
                    if (position != null) {
                        seekBar.setProgress(position.intValue());
                    }
                });
            } else {
                Log.e("PlayerFragment", "currentTrack == null");
            }

        });

        playerViewModel.isPlaying().observe(getViewLifecycleOwner(), isPlaying -> {
            playPauseButton.setImageResource(isPlaying ? R.drawable.ic_pause_24px : R.drawable.ic_play_arrow_24px);
        });

        playPauseButton.setOnClickListener(v -> {
            if (playerViewModel.isPlaying().getValue() != null && playerViewModel.isPlaying().getValue()) {
                playerViewModel.pauseTrack();
            } else {
                playerViewModel.resumeTrack();
            }
        });

        // Кнопка "Следующий трек"
        btnSkipNext.setOnClickListener(v -> playerViewModel.playNextTrack());

        // Кнопка "Предыдущий трек"
        btnSkipPrevious.setOnClickListener(v -> playerViewModel.playPreviousTrack());

        btn_favorite.setOnClickListener(v->{

        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    playerViewModel.getPlayerInstance().seekTo(progress);
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


    private void updateSeekBar() {
        handler.postDelayed(updateSeekBarRunnable, 1000);
    }

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

            // Восстанавливаем SeekBar
            Long duration = playerViewModel.getDuration().getValue();
            if (duration != null) {
                seekBar.setMax(duration.intValue());
            }
            Long position = playerViewModel.getCurrentPosition().getValue();
            if (position != null) {
                seekBar.setProgress(position.intValue());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();


    }
}
