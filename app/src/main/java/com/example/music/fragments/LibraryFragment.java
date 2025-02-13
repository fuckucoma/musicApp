package com.example.music.fragments;

import android.annotation.SuppressLint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.music.view_model.FeedPlayerViewModel;
import com.example.music.view_model.LibraryViewModel;
import com.example.music.PlaybackSource;
import com.example.music.repository.FavoriteRepository;
import com.example.music.activity.MainActivity;
import com.example.music.view_model.PlayerViewModel;
import com.example.music.activity.UploadTrackActivity;
import com.example.music.adapters.LibraryAdapter;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.Track;
import com.example.music.view_model.ProfileViewModel;
import com.example.test.BuildConfig;
import com.example.test.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class LibraryFragment extends Fragment {

    private static final String TAG = "LibraryFragment";

    private PlayerViewModel playerViewModel;
    private ApiService apiService;
    private FavoriteRepository favoriteRepository;
    private FeedPlayerViewModel feedPlayerViewModel;
    private ImageView profile;

    private RecyclerView recyclerView;
    private LibraryAdapter libraryAdapter;
    private List<Track> trackList = new ArrayList<>();
    private ProgressBar progressBar;
    private LibraryViewModel libraryViewModel;
    private ProfileViewModel profileViewModel;



    private List<Track> libraryTracks = new ArrayList<>();
    private Set<Integer> favoriteIds = new HashSet<>();

    private ActivityResultLauncher<Intent> uploadTrackLauncher;


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        libraryViewModel = new ViewModelProvider(this).get(LibraryViewModel.class);
        feedPlayerViewModel = new ViewModelProvider(requireActivity()).get(FeedPlayerViewModel.class);
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        apiService = ApiClient.getClient().create(ApiService.class);

        if (getActivity() instanceof MainActivity) {
            favoriteRepository = ((MainActivity) getActivity()).getFavoriteRepository();
        }

        recyclerView = view.findViewById(R.id.library_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        libraryAdapter = new LibraryAdapter(getContext(), new ArrayList<>(), this::onTrackSelected, this::onFavoriteClick);
        recyclerView.setAdapter(libraryAdapter);


        ImageView btnUploadTrack = view.findViewById(R.id.upload_track);
        profile = view.findViewById(R.id.profile_image);
        progressBar = view.findViewById(R.id.progress_bar);


        libraryViewModel.getLibraryTracks().observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null) {
                libraryTracks.clear();
                libraryTracks.addAll(tracks);
                updateAdapterData();
            }
        });

        libraryViewModel.getFavoriteTrackIds().observe(getViewLifecycleOwner(), ids -> {
            if (ids != null) {
                favoriteIds.clear();
                favoriteIds.addAll(ids);
                updateAdapterData();
            }
        });

        libraryViewModel.loadLibraryTracks();

        btnUploadTrack.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UploadTrackActivity.class);
            uploadTrackLauncher.launch(intent);
        });

        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {

                String imageUrl = userProfile.getProfileImageUrl();
                if (!TextUtils.isEmpty(imageUrl)) {
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_profile_24)
                            .into(profile);
                } else {
                    profile.setImageResource(R.drawable.ic_profile_24);
                }
            } else {
                profile.setImageResource(R.drawable.ic_profile_24);
            }
        });

        profile.setOnClickListener(v -> {
            DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawer_layout);
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        uploadTrackLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(getContext(), "Трек успешно загружен", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        return view;
    }

    private void updateAdapterData() {
        libraryAdapter.updateData(libraryTracks, favoriteIds);
        libraryAdapter.notifyDataSetChanged();
    }

    private void onFavoriteClick(Track track, boolean isFavorite) {
        if (isFavorite) {
            libraryViewModel.removeFromFavorites(track);
        } else {
            libraryViewModel.addToFavorites(track);
        }
//        libraryViewModel.refreshLibraryTracks();
    }

//    private void openUserProfile() {
//        Log.d(TAG, "Opening UserFragment");
//        ProfileFragment profileFragment = new ProfileFragment();
//        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.fragment_container, profileFragment);
//        transaction.addToBackStack(null);
//        transaction.commit();
//    }

    private void onTrackSelected(Track track) {
        String trackUrl = getTrackStreamUrl(track.getId() + "");
        feedPlayerViewModel.pauseFeedTrack();
        PlaybackSource source = PlaybackSource.LIBRARY;
        playerViewModel.playTrack(track, source);
    }

    private String getTrackStreamUrl(String trackId) {
        return BuildConfig.BASE_URL + "/tracks/" + trackId + "/stream";
    }
}