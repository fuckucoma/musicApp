package com.example.music.User_fragments;

import android.annotation.SuppressLint;

import android.content.Intent;
import android.os.Bundle;


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

import com.example.music.FavoriteRepository;
import com.example.music.MainActivity;
import com.example.music.PlayerViewModel;
import com.example.music.UploadTrackActivity;
import com.example.music.adapters.LibraryAdapter;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.FavoriteResponse;
import com.example.music.models.Track;
import com.example.test.BuildConfig;
import com.example.test.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    private RecyclerView recyclerView;
    private LibraryAdapter libraryAdapter;
    private List<Track> trackList = new ArrayList<>();
    private ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        // Инициализация ViewModel и API
        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        apiService = ApiClient.getClient().create(ApiService.class);

        // Инициализация FavoriteRepository из MainActivity
        if (getActivity() instanceof MainActivity) {
            favoriteRepository = ((MainActivity) getActivity()).getFavoriteRepository();
        }


        recyclerView = view.findViewById(R.id.library_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        libraryAdapter = new LibraryAdapter(getContext(), trackList, this::onTrackSelected, this::onFavoriteClick);
        recyclerView.setAdapter(libraryAdapter);


        Button btnUploadTrack = view.findViewById(R.id.upload_track);
        ImageView ivProfile = view.findViewById(R.id.ivProfile);
        progressBar = view.findViewById(R.id.progress_bar);


        btnUploadTrack.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UploadTrackActivity.class);
            startActivity(intent);
        });

        ivProfile.setOnClickListener(v -> openUserProfile());

        getFavorites();

        if (favoriteRepository != null) {
            favoriteRepository.getFavoriteTrackIds().observe(getViewLifecycleOwner(), favoriteIds -> {
                libraryAdapter.updateFavorites(favoriteIds);
                getFavorites();
            });
        }

        return view;
    }

    private void onFavoriteClick(Track track, boolean isFavorite) {
        if (favoriteRepository != null) {
            if (isFavorite) {
                favoriteRepository.removeTrackFromFavorites(track);
            } else {
                favoriteRepository.addTrackToFavorites(track);
            }
            favoriteRepository.getFavoriteTrackIds().observe(getViewLifecycleOwner(), favoriteIds -> {
                libraryAdapter.updateFavorites(new ArrayList<>(favoriteIds));
            });
        }
    }


    private void getFavorites() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getFavorites().enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    trackList.clear();
                    Set<Integer> favoriteIds = new HashSet<>();

                    for (FavoriteResponse.FavoriteTrack favoriteTrack : response.body().getFavoriteTracks()) {
                        Track track = new Track();
                        track.setId(favoriteTrack.getTrackId());
                        track.setTitle(favoriteTrack.getTitle());
                        track.setArtist(favoriteTrack.getArtist());
                        track.setImageUrl(favoriteTrack.getImageUrl());
                        track.setFilename(favoriteTrack.getFilename());
                        track.setCreatedAt(favoriteTrack.getCreatedAt());
                        trackList.add(track);



                        favoriteIds.add(favoriteTrack.getTrackId());
                    }

                    trackList.sort((track1, track2) -> {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                        try {
                            Date date1 = track1.getCreatedAt() != null ? dateFormat.parse(track1.getCreatedAt()) : null;
                            Date date2 = track2.getCreatedAt() != null ? dateFormat.parse(track2.getCreatedAt()) : null;

                            if (date1 == null && date2 == null) return 0;
                            if (date1 == null) return 1;
                            if (date2 == null) return -1;

                            return date2.compareTo(date1); // По убыванию
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0; // Если ошибка парсинга, не меняем порядок
                        }
                    });

                    libraryAdapter.updateData(trackList, favoriteIds);
                    libraryAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Не удалось получить избранное", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getContext(), "Ошибка при получении избранного", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
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
        Log.d(TAG, "Playing track URL: " + trackUrl);
        playerViewModel.playTrack(trackUrl, track);
    }

    private String getTrackStreamUrl(String trackId) {
        return BuildConfig.BASE_URL + "/tracks/" + trackId + "/stream";
    }
}