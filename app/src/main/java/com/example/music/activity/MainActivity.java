package com.example.music.activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.music.AdminPanel.AdminActivity;
import com.example.music.FavoriteManager;
import com.example.music.repository.FavoriteRepository;
import com.example.music.PlayerViewModel;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.fragments.HomeFragment;
import com.example.music.fragments.LibraryFragment;
import com.example.music.fragments.PlayerFragment;
import com.example.music.fragments.SearchFragment;
import com.example.music.models.Track;
import com.example.music.repository.TrackRepository;
import com.example.test.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {

    private PlayerViewModel playerViewModel;
    private View mediaPlayerBottomBar;
    private TextView trackTitleBar;
    private ImageView trackImageBar;
    private ImageButton playPauseButtonBar;
    private LinearProgressIndicator seekBar;
    private TextView bar_name_artist;
    private Handler handler = new Handler(Looper.getMainLooper());

    private FavoriteRepository favoriteRepository;


    private FavoriteManager favoriteManager;

    private Fragment currentFragment;

    private Fragment homeFragment = null;
    private Fragment searchFragment = null;
    private Fragment libraryFragment = null;

    private boolean isMediaBarAnimated = false;


    private boolean isPlayerFragmentVisible = false;
    private ApiService apiService;

    private String authToken;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        favoriteManager = new FavoriteManager();


        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        mediaPlayerBottomBar = findViewById(R.id.media_player_bottom_bar);
        trackTitleBar = findViewById(R.id.track_title_bar);
        trackImageBar = findViewById(R.id.track_image_bar);
        playPauseButtonBar = findViewById(R.id.play_pause_button_bar);
        seekBar = findViewById(R.id.seek_bar);
        bar_name_artist=findViewById(R.id.bar_name_artist);

        favoriteRepository = FavoriteRepository.getInstance(this);


        apiService = ApiClient.getClient().create(ApiService.class);

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", null);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

        Log.d("MainActivity", "Token найден: " + authToken);

        if (authToken == null) {
            openLoginActivity();
            finish();
            return;
        }

        this.authToken = authToken;

        if (isAdmin) {
            openAdminActivity();
            finish();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.navigation_home) {
                if (homeFragment == null) homeFragment = new HomeFragment();
                selectedFragment = homeFragment;
            } else if (item.getItemId() == R.id.navigation_search) {
                if (searchFragment == null) searchFragment = new SearchFragment();
                selectedFragment = searchFragment;
            } else if (item.getItemId() == R.id.navigation_library) {
                if (libraryFragment == null) libraryFragment = new LibraryFragment();
                selectedFragment = libraryFragment;
            }

            if (selectedFragment != null) {
                currentFragment = selectedFragment;

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();

                updateMediaPlayerVisibility();
            }
            return true;
        });

        setupMediaPlayerControls();

        if (savedInstanceState == null) {
            if (homeFragment == null) homeFragment = new HomeFragment();
            currentFragment = homeFragment;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        }


        favoriteRepository.fetchFavorites();
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        if ("ACTION_OPEN_PLAYER".equals(action)) {
            int trackId = intent.getIntExtra("TRACK_ID", -1);
            if (trackId != -1) {
                Track currentTrack = playerViewModel.getCurrentTrack().getValue();
                if (currentTrack != null && currentTrack.getId() == trackId) {
                    openPlayerFragment();
                    return;
                }
                Track newTrack = TrackRepository.getInstance().getTrackById(trackId);
                if (newTrack != null) {
                    playerViewModel.SetCurrentTrack(newTrack);
                }
                openPlayerFragment();
            }
        }
    }

    private void openAdminActivity() {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
    }

    private void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateMediaPlayerVisibility() {
        if (isPlayerFragmentVisible) {
            mediaPlayerBottomBar.setVisibility(View.GONE);
            return;
        }
        if (currentFragment instanceof HomeFragment) {
            mediaPlayerBottomBar.setVisibility(View.GONE);

            Log.d("MainActivity", "Media bar hidden in HomeFragment");
        } else if (playerViewModel.getCurrentTrack().getValue() != null) {
            if (!isMediaBarAnimated) {
                // Если анимация еще не выполнялась, запускаем её
                mediaPlayerBottomBar.setVisibility(View.VISIBLE);
                ObjectAnimator.ofFloat(mediaPlayerBottomBar, "translationY", mediaPlayerBottomBar.getHeight(), 0).start();
                isMediaBarAnimated = true; // Устанавливаем флаг
            } else {
                // Если анимация уже выполнялась, просто показываем панель
                mediaPlayerBottomBar.setVisibility(View.VISIBLE);
            }
        }
        else {
            mediaPlayerBottomBar.setVisibility(View.GONE);
            Log.d("MainActivity", "Media bar hidden, music not playing");
        }
    }

    private void setupMediaPlayerControls() {

        ImageButton favoriteButton = mediaPlayerBottomBar.findViewById(R.id.btn_favorite);


        playPauseButtonBar.setOnClickListener(v -> {
            if (playerViewModel.isPlaying().getValue() != null && playerViewModel.isPlaying().getValue()) {
                playerViewModel.pauseTrack();
                playPauseButtonBar.setImageResource(R.drawable.ic_play_arrow_24px);
            } else {
                playerViewModel.resumeTrack();
                playPauseButtonBar.setImageResource(R.drawable.ic_pause_24px);
            }
        });


        mediaPlayerBottomBar.setOnClickListener(v -> {
            Log.d("MainActivity", "Opening PlayerFragment");
            openPlayerFragment();
        });

        playerViewModel.getCurrentTrack().observe(this, track -> {
            if (track != null) {
                trackTitleBar.setText(track.getTitle());
                bar_name_artist.setText(track.getArtist());
                Picasso.get().load(track.getImageUrl()).into(trackImageBar);

                favoriteRepository.getFavoriteTrackIds().observe(this, favoriteIds -> {
                    if (favoriteIds.contains(track.getId())) {
                        favoriteButton.setImageResource(R.drawable.ic_heart__24);
                    } else {
                        favoriteButton.setImageResource(R.drawable.ic_favorite_24px);
                    }
                });

                updateFavoriteButtonState(track);
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

        favoriteRepository.getFavoriteTrackIds().observe(this, favoriteIds -> {
            Track currentTrack = playerViewModel.getCurrentTrack().getValue();
            if (currentTrack != null) {
                boolean isFavorite = favoriteIds.contains(currentTrack.getId());
                favoriteButton.setImageResource(isFavorite ? R.drawable.ic_heart__24 : R.drawable.ic_favorite_24px);
            }
        });

        playerViewModel.isPlaying().observe(this, isPlaying -> {
            updateMediaPlayerVisibility();
            if (isPlaying != null) {

                if (isPlaying) {

                    playPauseButtonBar.setImageResource(R.drawable.ic_pause_24px);
                    handler.post(updateRunnable);
                    updateSeekBar();
                } else {
                    playPauseButtonBar.setImageResource(R.drawable.ic_play_arrow_24px);
                    handler.removeCallbacks(updateRunnable);
                }
            }
        });

        playerViewModel.isPlayerReady().observe(this, isReady -> {
            if (isReady != null && isReady) {
                updateSeekBar();
            }
        });
    }

    private void updateFavoriteButtonState(Track track) {
        ImageButton favoriteButton = mediaPlayerBottomBar.findViewById(R.id.btn_favorite);
        boolean isFavorite = favoriteRepository.isTrackFavorite(track.getId());
        favoriteButton.setImageResource(isFavorite ? R.drawable.ic_heart__24 : R.drawable.ic_favorite_24px);
    }

    public FavoriteRepository getFavoriteRepository() {
        return favoriteRepository;
    }

    private void openPlayerFragment() {
        isPlayerFragmentVisible = true;
        mediaPlayerBottomBar.setVisibility(View.GONE);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.GONE);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new PlayerFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            isPlayerFragmentVisible = false;


            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            bottomNavigationView.setVisibility(View.VISIBLE);
            updateMediaPlayerVisibility();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPlayerFragmentVisible = getSupportFragmentManager().getBackStackEntryCount() > 0;
        updateMediaPlayerVisibility();
    }

    @Override
    protected void onPause() {
        super.onPause();

        updateMediaPlayerVisibility();
        handler.removeCallbacks(updateRunnable);

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
    protected void onDestroy() {
        super.onDestroy();
//        playerViewModel.releasePlayers();
        handler.removeCallbacks(updateRunnable);
    }
}
