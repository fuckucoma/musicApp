package com.example.music.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.music.FavoriteRepository;
import com.example.music.activity.MainActivity;
import com.example.music.PlayerViewModel;
import com.example.music.adapters.HomeAdapter;
import com.example.music.models.Track;
import com.example.music.response.TrackResponse;
import com.example.test.BuildConfig;
import com.example.test.R;
import com.google.gson.Gson;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private PlayerViewModel playerViewModel;
    private ViewPager2 viewPager;
    private HomeAdapter homeAdapter;
    private List<Track> trackList = new ArrayList<>();
    private FavoriteRepository favoriteRepository;
    private boolean isFetchingTracks = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        favoriteRepository = ((MainActivity) requireActivity()).getFavoriteRepository();


        viewPager = view.findViewById(R.id.view_pager);
        homeAdapter = new HomeAdapter(getContext(), trackList,favoriteRepository, this::onItemClicked, this::onFavoriteClicked);
        viewPager.setAdapter(homeAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                Log.d("HomeFragment", "Page selected: " + position);

                if (playerViewModel.isHomePlaying()) {
                    Track selectedTrack = trackList.get(position);

                    Log.d("HomeFragment", "Selected track: " + selectedTrack.getTitle());


                    if (playerViewModel.getCurrentHomeTrack() != null &&
                            playerViewModel.getCurrentHomeTrack().getId()==(selectedTrack.getId())) {
                        Log.d("HomeFragment", "Current track is already playing");
                        playTrack(selectedTrack);
                    } else {
                        Log.d("HomeFragment", "Playing new track");
                        playTrack(selectedTrack);
                    }
                }
            }
        });

        fetchTracks();

        return view;
    }

    private void onFavoriteClicked(Track track, boolean isFavorite) {
        if (isFavorite) {
            favoriteRepository.removeTrackFromFavorites(track);
            Toast.makeText(getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
            fetchTracks();
        } else {
            favoriteRepository.addTrackToFavorites(track);
            Toast.makeText(getContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
            fetchTracks();
        }
    }

    private void fetchTracks() {


        if (isFetchingTracks) {
            return;
        }

        isFetchingTracks = true;

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String url = BuildConfig.BASE_URL + "/tracks/";
            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                Log.d("HomeFragment", "Response Code: " + response.code());
                Log.d("HomeFragment", "Response Body: " + responseBody);

                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    TrackResponse.TrackData[] trackArray = gson.fromJson(responseBody, TrackResponse.TrackData[].class);

                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            trackList.clear();
                            for (TrackResponse.TrackData data : trackArray) {
                                trackList.add(new Track(
                                        data.getId(),
                                        data.getTitle(),
                                        data.getArtist(),
                                        data.getAlbum(),
                                        data.getImageUrl(),
                                        data.getFilename(),
                                        data.getCreatedAt()
                                ));
                            }
                            Collections.shuffle(trackList);
                            playerViewModel.setTrackList(trackList);
                            Log.d("PlayerViewModel", "setTrackList: trackList size = " + trackList.size());
                            homeAdapter.notifyDataSetChanged();
                        });
                    }
                } else {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Ошибка получения треков", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Ошибка сети: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
                e.printStackTrace();
            }
        }).start();
    }

    public void onItemClicked(Track track) {
        if (playerViewModel.isHomePlaying()) {

            playerViewModel.pauseHomeTrack();
        } else {

            playTrack(track);
        }
    }

    private void playTrack(Track track) {
        String trackUrl = getTrackStreamUrl(track.getId()+"");
        playerViewModel.playHomeTrack(trackUrl, track); // Используем playHomeTrack
    }

    @Override
    public void onPause() {
        super.onPause();
        if (playerViewModel.isHomePlaying()) {
            playerViewModel.pauseHomeTrack();
        }
    }

    private String getTrackStreamUrl(String trackId) {
        return BuildConfig.BASE_URL + "/tracks/" + trackId + "/stream";
    }
}
