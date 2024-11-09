package com.example.music;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.music.fragments.HomeFragment;
import com.example.music.fragments.LibraryFragment;
import com.example.music.fragments.SearchFragment;
import com.example.test.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {

    private PlayerViewModel playerViewModel;
    private ExoPlayer exoPlayer;
    private View mediaPlayerBottomBar;
    private TextView trackTitleBar;
    private ImageView trackImageBar;
    private ImageButton playPauseButtonBar;
    private SeekBar seekBar;
    private Handler handler = new Handler(Looper.getMainLooper());

    private Fragment currentFragment;

    private Fragment homeFragment = null;
    private Fragment searchFragment = null;
    private Fragment libraryFragment = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        exoPlayer = new ExoPlayer.Builder(this).build();
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        playerViewModel.setPlayer(exoPlayer);

        mediaPlayerBottomBar = findViewById(R.id.media_player_bottom_bar);
        trackTitleBar = findViewById(R.id.track_title_bar);
        trackImageBar = findViewById(R.id.track_image_bar);
        playPauseButtonBar = findViewById(R.id.play_pause_button_bar);
        seekBar = findViewById(R.id.seek_bar);

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
        if (currentFragment instanceof HomeFragment) {
            mediaPlayerBottomBar.setVisibility(View.GONE);
            Log.d("MainActivity", "Media bar hidden in HomeFragment");
        } else if (playerViewModel.isPlaying().getValue() != null && playerViewModel.isPlaying().getValue()) {
            mediaPlayerBottomBar.setVisibility(View.VISIBLE);
            Log.d("MainActivity", "Media bar shown in other fragment");
        } else {
            mediaPlayerBottomBar.setVisibility(View.GONE);
            Log.d("MainActivity", "Media bar hidden, music not playing");
        }
    }

    private boolean isInHomeFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        return currentFragment instanceof HomeFragment;
    }

    private void setupMediaPlayerControls() {
        playPauseButtonBar.setOnClickListener(v -> {
            if (playerViewModel.isPlaying().getValue() != null && playerViewModel.isPlaying().getValue()) {
                playerViewModel.pauseTrack();
                playPauseButtonBar.setImageResource(R.drawable.ic_play);
            } else {
                playerViewModel.getPlayer().play();
                playPauseButtonBar.setImageResource(R.drawable.ic_pause);
            }
        });

        playerViewModel.getCurrentTrack().observe(this, track -> {
            updateMediaPlayerVisibility();

            if (track != null && mediaPlayerBottomBar.getVisibility() == View.VISIBLE) {
                trackTitleBar.setText(track.getTitle());
                Picasso.get().load(track.getImageUrl()).into(trackImageBar);
                updateSeekBar();
            }
        });

        playerViewModel.isPlaying().observe(this, isPlaying -> {
            updateMediaPlayerVisibility();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMediaPlayerVisibility();
    }

    private void updateSeekBar() {
        seekBar.setMax((int) playerViewModel.getPlayer().getDuration());
        handler.postDelayed(updateRunnable, 1000);
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (playerViewModel.getPlayer().isPlaying()) {
                seekBar.setProgress((int) playerViewModel.getPlayer().getCurrentPosition());
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayer.release();
        handler.removeCallbacks(updateRunnable);
    }
}
