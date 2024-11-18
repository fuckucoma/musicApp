package com.example.music.User_fragments;

// PlayerFragment.java
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.test.R;
import com.squareup.picasso.Picasso;

public class PlayerFragment extends Fragment {

    private ImageView albumArt;
    private TextView trackTitle;
    private SeekBar seekBar;
    private ImageButton playPauseButton;
    private ImageButton btn_favorite;

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

        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);

        setupPlayerControls();

        return view;
    }

    private void setupPlayerControls() {
        // Наблюдаем за изменениями текущего трека
        playerViewModel.getCurrentTrack().observe(getViewLifecycleOwner(), track -> {
            if (track != null) {
                trackTitle.setText(track.getTitle());
                Picasso.get().load(track.getImageUrl()).into(albumArt);
                seekBar.setMax((int) playerViewModel.getPlayerInstance().getDuration());
                updateSeekBar();
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
        seekBar.setProgress((int) playerViewModel.getPlayerInstance().getCurrentPosition());
        handler.postDelayed(updateSeekBarRunnable, 1000);
    }

    private Runnable updateSeekBarRunnable = this::updateSeekBar;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateSeekBarRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();


    }
}
