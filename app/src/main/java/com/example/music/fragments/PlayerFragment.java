package com.example.music.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music.adapters.ReviewAdapter;
import com.example.music.models.Review;
import com.example.music.models.User;
import com.example.music.repository.ProfileRepository;
import com.example.music.repository.ReviewRepository;
import com.example.music.response.UserProfileResponse;
import com.example.music.view_model.PlayerViewModel;
import com.example.music.activity.MainActivity;
import com.example.music.models.Track;
import com.example.music.repository.FavoriteRepository;
import com.example.music.ui.TrackOptionsBottomSheet;
import com.example.music.view_model.ProfileViewModel;
import com.example.music.view_model.ReviewViewModel;
import com.example.test.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PlayerFragment extends Fragment {

    private ImageView albumArt;
    private TextView trackTitle, name_artist, current_duration, song_max_duration;
    private SeekBar seekBar;
    private FloatingActionButton playPauseButton;
    private ImageButton btn_favorite, btn_more, btn_back;
    private ExtendedFloatingActionButton btnSkipPrevious;
    private ExtendedFloatingActionButton btnSkipNext;
    private ExtendedFloatingActionButton repeat_btn;
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();
    private ReviewViewModel reviewViewModel;
    private ProfileViewModel profileViewModel;

    private PlayerViewModel playerViewModel;
    private FavoriteRepository favoriteRepository;
    private boolean isRepeatEnabled = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    private int currentUserId = -1;

    private Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            Long currentPosition = playerViewModel.getCurrentPosition().getValue();
            if (currentPosition != null) {
                seekBar.setProgress(currentPosition.intValue());
            }
            if (playerViewModel.isPlaying().getValue() != null && playerViewModel.isPlaying().getValue()) {
                handler.postDelayed(this, 1000);
            }
        }
    };


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
        current_duration = view.findViewById(R.id.current_duration);
        song_max_duration = view.findViewById(R.id.song_max_duration);
        repeat_btn = view.findViewById(R.id.btn_repeat);
        btn_more = view.findViewById(R.id.btn_more);
        btn_back = view.findViewById(R.id.btn_back);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        reviewsRecyclerView = view.findViewById(R.id.reviews_recyclerview);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);

        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);

        if (requireActivity() instanceof MainActivity) {
            favoriteRepository = ((MainActivity) requireActivity()).getFavoriteRepository();
        } else {

            favoriteRepository = FavoriteRepository.getInstance(requireContext());
        }

        setupPlayerControls();



        return view;
    }

    private void setupPlayerControls() {


        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                currentUserId = profile.getId();
                // Теперь у нас есть ID пользователя
                Log.d("SomeFragment", "Текущий userId: " + currentUserId);

                reviewAdapter = new ReviewAdapter(getContext(), reviewList, currentUserId);
                reviewsRecyclerView.setAdapter(reviewAdapter);

                reviewViewModel.getReviewsLiveData().observe(getViewLifecycleOwner(), reviews -> {
                    reviewList.clear();
                    reviewList.addAll(reviews);
                    reviewAdapter.notifyDataSetChanged();
                });
            }
        });



        playerViewModel.getCurrentTrack().observe(getViewLifecycleOwner(), track -> {
            if (track != null) {
                trackTitle.setText(track.getTitle());
                name_artist.setText(track.getArtist());
                Glide.with(this).load(track.getImageUrl()).into(albumArt);
                reviewViewModel.fetchReviewsForTrack(track.getId());

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

                updateFavoriteButton(track);
            } else {
                Log.e("PlayerFragment", "currentTrack == null");
                trackTitle.setText("");
                name_artist.setText("");
                albumArt.setImageResource(R.drawable.placeholder_image);
                seekBar.setMax(0);
                seekBar.setProgress(0);
                current_duration.setText("00:00");
                song_max_duration.setText("00:00");
                btn_favorite.setImageResource(R.drawable.ic_favorite_24px);
            }
        });

        playerViewModel.isPlaying().observe(getViewLifecycleOwner(), isPlaying -> {
            playPauseButton.setImageResource(isPlaying ? R.drawable.ic_pause_24px : R.drawable.ic_play_arrow_24px);
        });

        favoriteRepository.getFavoriteTrackIds().observe(getViewLifecycleOwner(), ids -> {
            Track currentTrack = playerViewModel.getCurrentTrack().getValue();
            if (currentTrack != null) {
                updateFavoriteButton(currentTrack);
            }
        });

        playPauseButton.setOnClickListener(v -> {
            Boolean playing = playerViewModel.isPlaying().getValue();
            if (playing != null && playing) {
                playerViewModel.pauseTrack();
            } else {
                playerViewModel.resumeTrack();
            }
        });

        btn_more.setOnClickListener(v -> {
            Track currentTrack = playerViewModel.getCurrentTrack().getValue();
            int trackId = currentTrack.getId();
            TrackOptionsBottomSheet bottomSheet = TrackOptionsBottomSheet.newInstance(trackId);
            bottomSheet.show(getParentFragmentManager(), "TrackOptionsBottomSheet");
        });

        btn_back.setOnClickListener(v->{
            NavController navController = NavHostFragment.findNavController(PlayerFragment.this);
            navController.popBackStack();
        });

        playerViewModel.isRepeatModeEnabled().observe(getViewLifecycleOwner(), isEnabled -> {
            repeat_btn.setIconResource(isEnabled ? R.drawable.ic_repeat_one_24px : R.drawable.ic_repeat_24px);
        });

        repeat_btn.setOnClickListener(v -> playerViewModel.toggleRepeatMode());

        btnSkipNext.setOnClickListener(v -> playerViewModel.playNextTrack());
        btnSkipPrevious.setOnClickListener(v -> playerViewModel.playPreviousTrack());

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
                song_max_duration.setText(formatTime(duration));
            }
        });

        playerViewModel.getCurrentPosition().observe(getViewLifecycleOwner(), position -> {
            if (position != null) {
                seekBar.setProgress(position.intValue());
                current_duration.setText(formatTime(position));
            }
        });

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
