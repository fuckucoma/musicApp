package com.example.music.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.music.AdminPanel.AdminActivity;
import com.example.music.fragments.MediaBarFragment;
import com.example.music.repository.FavoriteRepository;
import com.example.music.service.MusicService;
import com.example.music.ui.AppNavigationManager;
import com.example.music.ui.DrawerHeaderManager;
import com.example.music.view_model.PlayerViewModel;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.Track;
import com.example.music.repository.TrackRepository;
import com.example.music.view_model.ProfileViewModel;
import com.example.test.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity
        implements MediaBarFragment.MediaBarListener {

    private static final String TAG = "MainActivity";

    private PlayerViewModel playerViewModel;
    private ProfileViewModel profileViewModel;
    private FavoriteRepository favoriteRepository;

    private AppNavigationManager appNavigationManager;
    private DrawerHeaderManager drawerHeaderManager;

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate()");

        // 1) Инициализируем ViewModel-ы и Repos
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        favoriteRepository = FavoriteRepository.getInstance(this);

        // 2) Находим DrawerLayout, NavigationView, BottomNav и NavController
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // 3) Создаём менеджеры
        appNavigationManager = new AppNavigationManager(
                this,
                drawerLayout,
                navView,
                bottomNav,
                navController,
                playerViewModel,
                favoriteRepository
        );

        drawerHeaderManager = new DrawerHeaderManager(
                this,
                navController,
                navView,
                profileViewModel,
                playerViewModel,
                drawerLayout,
                favoriteRepository
        );

        // 4) Настраиваем менеджеры
        appNavigationManager.setupNavigation();
        drawerHeaderManager.setupDrawer();

        // 5) Проверка токена, admin
        SharedPreferences sp = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String authToken = sp.getString("authToken", null);
        boolean isAdmin = sp.getBoolean("isAdmin", false);

        if (authToken == null) {
            openLoginActivity();
            finish();
            return;
        }
        if (isAdmin) {
            openAdminActivity();
            finish();
            return;
        }

        // 6) Загружаем избранное + профиль
        favoriteRepository.fetchFavorites();
        profileViewModel.fetchUserProfile();

        // 7) Добавляем MediaBarFragment, если ещё нет
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.media_bar_container, new MediaBarFragment())
                    .commit();
        }

        // 8) Обработка Intent (например, ACTION_OPEN_PLAYER)
        handleIntent(getIntent());
    }

    @Override
    public void onMediaBarClicked() {
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        navController.navigate(R.id.playerFragment);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if ("ACTION_OPEN_PLAYER".equals(action)) {
            int trackId = intent.getIntExtra("TRACK_ID", -1);
            if (trackId != -1) {
                Track currentTrack = playerViewModel.getCurrentTrack().getValue();
                if (currentTrack != null && currentTrack.getId() == trackId) {
                    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.nav_host_fragment);
                    NavController navController = navHostFragment.getNavController();

                    navController.navigate(R.id.playerFragment);
                    return;
                }
                Track newTrack = TrackRepository.getInstance().getTrackById(trackId);
                if (newTrack != null) {
                    playerViewModel.SetCurrentTrack(newTrack);
                }
                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
                NavController navController = navHostFragment.getNavController();
                navController.navigate(R.id.playerFragment);
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

    public FavoriteRepository getFavoriteRepository() {
        return favoriteRepository;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            NavHostFragment navHostFragment = (NavHostFragment)
                    getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            NavController navController = navHostFragment.getNavController();
            if (!navController.popBackStack()) {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
