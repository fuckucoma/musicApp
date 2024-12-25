package com.example.music.fragments;

import android.annotation.SuppressLint;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;


import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.music.FeedPlayerViewModel;
import com.example.music.LibraryViewModel;
import com.example.music.PlaybackSource;
import com.example.music.repository.FavoriteRepository;
import com.example.music.activity.MainActivity;
import com.example.music.PlayerViewModel;
import com.example.music.activity.UploadTrackActivity;
import com.example.music.adapters.LibraryAdapter;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.response.FavoriteResponse;
import com.example.music.models.Track;
import com.example.music.service.MusicService;
import com.example.test.BuildConfig;
import com.example.test.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LibraryFragment extends Fragment {

    private static final String TAG = "LibraryFragment";

    private PlayerViewModel playerViewModel;
    private ApiService apiService;
    private FavoriteRepository favoriteRepository;
    private FeedPlayerViewModel feedPlayerViewModel;

    private RecyclerView recyclerView;
    private LibraryAdapter libraryAdapter;
    private List<Track> trackList = new ArrayList<>();
    private ProgressBar progressBar;
    private LibraryViewModel libraryViewModel;

    private List<Track> libraryTracks = new ArrayList<>();
    private Set<Integer> favoriteIds = new HashSet<>();


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        // Инициализация ViewModel и API
        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        libraryViewModel = new ViewModelProvider(this).get(LibraryViewModel.class);
        feedPlayerViewModel = new ViewModelProvider(requireActivity()).get(FeedPlayerViewModel.class);
        apiService = ApiClient.getClient().create(ApiService.class);

        // Инициализация FavoriteRepository из MainActivity
        if (getActivity() instanceof MainActivity) {
            favoriteRepository = ((MainActivity) getActivity()).getFavoriteRepository();
        }

        recyclerView = view.findViewById(R.id.library_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        libraryAdapter = new LibraryAdapter(getContext(), new ArrayList<>(), this::onTrackSelected, this::onFavoriteClick);
        recyclerView.setAdapter(libraryAdapter);


        Button btnUploadTrack = view.findViewById(R.id.upload_track);
        ImageView ivProfile = view.findViewById(R.id.ivProfile);
        progressBar = view.findViewById(R.id.progress_bar);


        libraryViewModel.getLibraryTracks().observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null) {
                libraryTracks.clear();
                libraryTracks.addAll(tracks);
                updateAdapterData();
            }
        });

        // Подписываемся на избранные ID
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
            startActivity(intent);
        });

        ivProfile.setOnClickListener(v -> openUserProfile());

        return view;
    }

    private void updateAdapterData() {
        libraryAdapter.updateData(libraryTracks, favoriteIds);
//        playerViewModel.setTrackList(currentTracks);
        //playerViewModel.setTrackList(currentTracks, PlaybackSource.LIBRARY);
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

    private void openUserProfile() {
        Log.d(TAG, "Opening UserFragment");
        UserFragment userFragment = new UserFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, userFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

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