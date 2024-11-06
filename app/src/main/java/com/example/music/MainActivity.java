package com.example.music;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.music.fragments.HomeFragment;
import com.example.music.fragments.LibraryFragment;
import com.example.music.fragments.SearchFragment;
import com.example.test.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private PlayerViewModel playerViewModel;
    private ExoPlayer exoPlayer;

    Fragment homeFragment = null;
    Fragment searchFragment = null;
    Fragment libraryFragment = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ExoPlayer and ViewModel
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        playerViewModel.setPlayer(exoPlayer);

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
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set default fragment on launch
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayer.release();
    }
}
