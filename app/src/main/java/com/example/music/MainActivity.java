package com.example.music;

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
import android.widget.Toast;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.api.FavoriteResponse;
import com.example.music.fragments.HomeFragment;
import com.example.music.fragments.LibraryFragment;
import com.example.music.fragments.PlayerFragment;
import com.example.music.fragments.SearchFragment;
import com.example.music.models.Track;
import com.example.test.R;
import com.google.android.exoplayer2.C;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private PlayerViewModel playerViewModel;
    private View mediaPlayerBottomBar;
    private TextView trackTitleBar;
    private ImageView trackImageBar;
    private ImageButton playPauseButtonBar;
    private LinearProgressIndicator seekBar;
    private Handler handler = new Handler(Looper.getMainLooper());




    private Fragment currentFragment;

    private Fragment homeFragment = null;
    private Fragment searchFragment = null;
    private Fragment libraryFragment = null;


    private boolean isPlayerFragmentVisible = false;
    private ApiService apiService;

    private String authToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        mediaPlayerBottomBar = findViewById(R.id.media_player_bottom_bar);
        trackTitleBar = findViewById(R.id.track_title_bar);
        trackImageBar = findViewById(R.id.track_image_bar);
        playPauseButtonBar = findViewById(R.id.play_pause_button_bar);
        seekBar = findViewById(R.id.seek_bar);

        apiService = ApiClient.getClient().create(ApiService.class);

        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", null);
        Log.d("MainActivity", "Token user: " + authToken);


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
            mediaPlayerBottomBar.setVisibility(View.VISIBLE);
            Log.d("MainActivity", "Media bar shown in other fragment");
        } else {
            mediaPlayerBottomBar.setVisibility(View.GONE);
            Log.d("MainActivity", "Media bar hidden, music not playing");
        }
    }

    private void setupMediaPlayerControls() {
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
            updateMediaPlayerVisibility();

            if (track != null && mediaPlayerBottomBar.getVisibility() == View.VISIBLE) {
                trackTitleBar.setText(track.getTitle());
                Picasso.get().load(track.getImageUrl()).into(trackImageBar);
                mediaPlayerBottomBar.findViewById(R.id.btn_favorite).setOnClickListener(v -> {
                    Log.d("Track",": " + track);
                    addTrackToFavorites(track);
                });
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

    private void addTrackToFavorites(Track track) {
        FavoriteRequest favoriteRequest = new FavoriteRequest(track.getId());
        apiService.addFavorite(favoriteRequest).enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                        Log.d("Лайки", "Трек добавлен в избранное: " + response.body());
                    }
                    else {
                        Log.d("Лайки", "Ошибка добавления в избранное: " + response.message() );
                    }
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                Log.d("Лайки", "Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void openPlayerFragment() {
        isPlayerFragmentVisible = true; // Устанавливаем флаг
        mediaPlayerBottomBar.setVisibility(View.GONE);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new PlayerFragment())
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            isPlayerFragmentVisible = false; // Сбрасываем флаг, если мы вышли из PlayerFragment
            updateMediaPlayerVisibility();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMediaPlayerVisibility();
    }

    private void updateSeekBar() {
        if (playerViewModel.getPlayerInstance() != null) {
            long duration = playerViewModel.getPlayerInstance().getDuration();
            if (duration != C.TIME_UNSET && duration > 0) {
                seekBar.setMax((int) duration);

                handler.postDelayed(updateRunnable, 1000);
            }
            else {
                handler.postDelayed(this::updateSeekBar, 500);
            }
        }
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (playerViewModel.getPlayerInstance() != null && playerViewModel.getPlayerInstance().isPlaying()) {

                long currentPosition = playerViewModel.getPlayerInstance().getCurrentPosition();
                long duration = playerViewModel.getPlayerInstance().getDuration();

                if (duration != C.TIME_UNSET && duration > 0) {
                    seekBar.setMax((int) duration);
                    seekBar.setProgress((int) currentPosition);
                    handler.postDelayed(this, 1000);
                }
            }
            else{
                handler.removeCallbacks(updateRunnable);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerViewModel.releasePlayers();
        handler.removeCallbacks(updateRunnable);
    }
}
