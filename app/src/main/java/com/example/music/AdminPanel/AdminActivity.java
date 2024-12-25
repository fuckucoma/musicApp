package com.example.music.AdminPanel;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.music.fragments.UserFragment;
import com.example.test.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {


    private Fragment adminTracksFragment = null;
    private Fragment userFragment = null;
    private Fragment usersFragment = null;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_admin);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.navigation_tracks) {
                if (adminTracksFragment == null) adminTracksFragment = new AdminTracks();
                selectedFragment = adminTracksFragment;
            } else
            if (item.getItemId() == R.id.navigation_users) {
                if (usersFragment == null) usersFragment = new AdminUsers();
                selectedFragment = usersFragment;
            } else if (item.getItemId() == R.id.navigation_profile) {
                if (userFragment == null) userFragment = new UserFragment();
                selectedFragment = userFragment;
            }

            if (selectedFragment != null) {
                currentFragment = selectedFragment;

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            if (savedInstanceState == null) {
                if (adminTracksFragment == null) adminTracksFragment = new AdminTracks();
                currentFragment = adminTracksFragment;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,adminTracksFragment)
                        .commit();
            }

            return true;
        });

    }
}
