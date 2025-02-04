package com.example.music.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavController;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.music.activity.LoginActivity;
import com.example.music.service.MusicService;
import com.example.music.view_model.PlayerViewModel;
import com.example.music.repository.FavoriteRepository;
import com.example.music.view_model.ProfileViewModel;
import com.example.test.R;
import com.google.android.material.navigation.NavigationView;

public class DrawerHeaderManager {

    private final Activity activity;
    private final NavController navController;
    private final NavigationView navigationView;
    private final ProfileViewModel profileViewModel;
    private final PlayerViewModel playerViewModel;
    private final DrawerLayout drawerLayout;
    private final FavoriteRepository favoriteRepository;

    private final SharedPreferences sharedPreferences;

    public DrawerHeaderManager(
            Activity activity,
            NavController navController,
            NavigationView navigationView,
            ProfileViewModel profileViewModel,
            PlayerViewModel playerViewModel,
            DrawerLayout drawerLayout,
            FavoriteRepository favoriteRepository
    ) {
        this.activity = activity;
        this.navController = navController;
        this.navigationView = navigationView;
        this.profileViewModel = profileViewModel;
        this.playerViewModel = playerViewModel;
        this.drawerLayout = drawerLayout;
        this.favoriteRepository = favoriteRepository;

        this.sharedPreferences = activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
    }

    public void setupDrawer() {
        View headerView = navigationView.getHeaderView(0);
        LinearLayout headerLinearLayout = headerView.findViewById(R.id.header_linear_layout);
        ImageView profileImageView = headerView.findViewById(R.id.profile_image);
        TextView userNameTextView = headerView.findViewById(R.id.user_name);

        profileViewModel.getUserProfile().observe((LifecycleOwner) activity, userProfile -> {
            if (userProfile != null) {
                userNameTextView.setText(userProfile.getUsername());

                String imageUrl = userProfile.getProfileImageUrl();
                if (!TextUtils.isEmpty(imageUrl)) {
                    Glide.with(activity)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_profile_24)
                            .into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.ic_profile_24);
                }
            } else {
                userNameTextView.setText("Unknown user");
                profileImageView.setImageResource(R.drawable.ic_profile_24);
            }
        });

        headerLinearLayout.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);

            if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == R.id.profileFragment) {
                return;
            }
            navController.navigate(R.id.profileFragment);
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);

            if (item.getItemId() == R.id.nav_exit) {
                logoutUser();
                return true;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }

    private void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("authToken");
        editor.remove("isAdmin");
        editor.remove("history");
        editor.apply();

        Intent stopServiceIntent = new Intent(activity, MusicService.class);
        stopServiceIntent.setAction("STOP_SERVICE");
        activity.startService(stopServiceIntent);

        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }
}
