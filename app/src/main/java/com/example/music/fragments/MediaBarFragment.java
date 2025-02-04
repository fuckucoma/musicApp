package com.example.music.fragments;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.test.R;
import com.example.music.models.Track;
import com.example.music.repository.FavoriteRepository;
import com.example.music.view_model.PlayerViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.squareup.picasso.Picasso;

public class MediaBarFragment extends Fragment {

    private static final String TAG = "MediaBarFragment";

    private PlayerViewModel playerViewModel;
    private FavoriteRepository favoriteRepository;

    private ImageView trackImageBar;
    private TextView trackTitleBar;
    private TextView barNameArtist;
    private ImageButton playPauseButtonBar;
    private ImageButton favoriteButton;
    private LinearProgressIndicator seekBar;

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isMediaBarAnimated = false;

    public MediaBarFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_media_player_bar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated()");
        trackImageBar = view.findViewById(R.id.track_image_bar);
        trackTitleBar = view.findViewById(R.id.track_title_bar);
        barNameArtist = view.findViewById(R.id.bar_name_artist);
        playPauseButtonBar = view.findViewById(R.id.play_pause_button_bar);
        favoriteButton = view.findViewById(R.id.btn_favorite);
        seekBar = view.findViewById(R.id.seek_bar);

        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        favoriteRepository = FavoriteRepository.getInstance(requireContext());

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        playerViewModel.getCurrentTrack().observe(getViewLifecycleOwner(), track -> {
            if (track != null) {
                trackTitleBar.setText(track.getTitle());
                barNameArtist.setText(track.getArtist());
                Glide.with(this).load(track.getImageUrl()).into(trackImageBar);

                updateFavoriteIcon(track);
            }
        });

        playerViewModel.isPlaying().observe(getViewLifecycleOwner(), isPlaying -> {
            if (isPlaying != null && isPlaying) {
                playPauseButtonBar.setImageResource(R.drawable.ic_pause_24px);
                handler.post(updateRunnable);
                updateSeekBar();
            } else {
                playPauseButtonBar.setImageResource(R.drawable.ic_play_arrow_24px);
                handler.removeCallbacks(updateRunnable);
            }
        });

        playerViewModel.isPlayerReady().observe(getViewLifecycleOwner(), isReady -> {
            if (isReady != null && isReady) {
                updateSeekBar();
            }
        });

        favoriteRepository.getFavoriteTrackIds().observe(getViewLifecycleOwner(), favoriteIds -> {
            Track currentTrack = playerViewModel.getCurrentTrack().getValue();
            if (currentTrack != null) {
                updateFavoriteIcon(currentTrack);
            }
        });
    }

    private void setupListeners() {
        playPauseButtonBar.setOnClickListener(v -> {
            Boolean isPlaying = playerViewModel.isPlaying().getValue();
            if (isPlaying != null && isPlaying) {
                playerViewModel.pauseTrack();
            } else {
                playerViewModel.resumeTrack();
            }
        });

        favoriteButton.setOnClickListener(v -> {
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

        // При клике на сам бар -> открыть playerFragment
        // Можно вызвать callback в Activity, если Activity реализует MediaBarListener
        getView().setOnClickListener(v -> {
            if (getActivity() instanceof MediaBarListener) {
                ((MediaBarListener) getActivity()).onMediaBarClicked();
            }
        });
    }

    private void updateFavoriteIcon(Track track) {
        boolean isFavorite = favoriteRepository.isTrackFavorite(track.getId());
        favoriteButton.setImageResource(
                isFavorite ? R.drawable.ic_heart__24 : R.drawable.ic_favorite_24px
        );
    }

    private void updateSeekBar() {
        Long duration = playerViewModel.getDuration().getValue();
        if (duration != null && duration > 0) {
            seekBar.setMax(duration.intValue());
            handler.postDelayed(updateRunnable, 1000);
        } else {
            handler.postDelayed(this::updateSeekBar, 500);
        }
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Boolean isPlaying = playerViewModel.isPlaying().getValue();
            if (isPlaying != null && isPlaying) {
                Long currentPosition = playerViewModel.getCurrentPosition().getValue();
                Long duration = playerViewModel.getDuration().getValue();
                if (duration != null && duration > 0) {
                    seekBar.setMax(duration.intValue());
                    if (currentPosition != null) {
                        seekBar.setProgress(currentPosition.intValue());
                    }
                    handler.postDelayed(this, 1000);
                }
            } else {
                handler.removeCallbacks(this);
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateRunnable);
    }

    public interface MediaBarListener {
        void onMediaBarClicked();
    }
}
