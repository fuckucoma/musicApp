package com.example.music.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavController;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.example.music.activity.LoginActivity;
import com.example.music.models.Track;
import com.example.music.repository.FavoriteRepository;
import com.example.music.service.MusicService;
import com.example.music.view_model.PlayerViewModel;
import com.example.music.view_model.ProfileViewModel;
import com.example.test.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class AppNavigationManager {

    private final Activity activity;
    private final DrawerLayout drawerLayout;
    private final NavigationView navigationView;
    private final BottomNavigationView bottomNavigationView;
    private final NavController navController;
    private final PlayerViewModel playerViewModel;
    private final FavoriteRepository favoriteRepository;

    public AppNavigationManager(Activity activity,
                                DrawerLayout drawerLayout,
                                NavigationView navigationView,
                                BottomNavigationView bottomNavigationView,
                                NavController navController,
                                PlayerViewModel playerViewModel,
                                FavoriteRepository favoriteRepository) {
        this.activity = activity;
        this.drawerLayout = drawerLayout;
        this.navigationView = navigationView;
        this.bottomNavigationView = bottomNavigationView;
        this.navController = navController;
        this.playerViewModel = playerViewModel;
        this.favoriteRepository = favoriteRepository;
    }

    public void setupNavigation() {
        // 1) Привязываем bottomNav к NavController
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // 2) OnDestinationChangedListener
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();
            if (destId == R.id.playerFragment) {
                bottomNavigationView.setVisibility(View.GONE);
                activity.findViewById(R.id.media_bar_container).setVisibility(View.GONE);
            } else if (destId == R.id.homeFragment) {
                bottomNavigationView.setVisibility(View.VISIBLE);
                activity.findViewById(R.id.media_bar_container).setVisibility(View.GONE);
            } else if (destId == R.id.profileFragment) {
                bottomNavigationView.setVisibility(View.GONE);
                activity.findViewById(R.id.media_bar_container).setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
                Track currentTrack = playerViewModel.getCurrentTrack().getValue();
                if (currentTrack != null) {
                    activity.findViewById(R.id.media_bar_container).setVisibility(View.VISIBLE);
                } else {
                    activity.findViewById(R.id.media_bar_container).setVisibility(View.GONE);
                }
            }
        });

        // 3) При изменении currentTrack -> если мы не в Player/Home, показываем медиабар
        playerViewModel.getCurrentTrack().observe((LifecycleOwner) activity, track -> {
            int destId = navController.getCurrentDestination().getId();
            if (track != null && destId != R.id.playerFragment && destId != R.id.homeFragment) {
                activity.findViewById(R.id.media_bar_container).setVisibility(View.VISIBLE);
            }
        });
    }
}
