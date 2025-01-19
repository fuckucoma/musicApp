package com.example.music.AdminPanel;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.test.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment currentFragment;

    // Заранее создаём переменные под фрагменты
    private AdminDashboardFragment dashboardFragment;
    private ComplaintFragment complaintFragment;
    private ReviewFragment reviewFragment;
    private TrackManagmentFragment trackManagmentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        bottomNavigationView = findViewById(R.id.admin_bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.navigation_dashboard) {
                if (dashboardFragment == null) {
                    dashboardFragment = new AdminDashboardFragment();
                }
                selectedFragment = dashboardFragment;
            } else if (item.getItemId() == R.id.navigation_complaints) {
                if (complaintFragment == null) {
                    complaintFragment = new ComplaintFragment();
                }
                selectedFragment = complaintFragment;
            } else if (item.getItemId() == R.id.navigation_reviews) {
                if (reviewFragment == null) {
                    reviewFragment = new ReviewFragment();
                }
                selectedFragment = reviewFragment;
            } else if (item.getItemId() == R.id.navigation_tracks) {
                if (trackManagmentFragment == null) {
                    trackManagmentFragment = new TrackManagmentFragment();
                }
                selectedFragment = trackManagmentFragment;
            }

            if (selectedFragment != null) {
                currentFragment = selectedFragment;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.admin_fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        if (savedInstanceState == null) {
            dashboardFragment = new AdminDashboardFragment();
            currentFragment = dashboardFragment;

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_fragment_container, dashboardFragment)
                    .commit();
        }
    }
}
