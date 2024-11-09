package com.example.music.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.music.PlayerViewModel;
import com.example.music.adapters.HomeAdapter;
import com.example.music.models.Track;
import com.example.music.models.TrackResponse;
import com.example.test.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private PlayerViewModel playerViewModel;
    private ViewPager2 viewPager;
    private HomeAdapter homeAdapter;
    private List<Track> trackList = new ArrayList<>();
    private boolean isPlayingStarted = false;
    private boolean isTrackPlaying = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);

        viewPager = view.findViewById(R.id.view_pager);
        homeAdapter = new HomeAdapter(getContext(), trackList, this::onItemClicked);
        viewPager.setAdapter(homeAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (playerViewModel.isPlaying().getValue() != null && playerViewModel.isPlaying().getValue()
                        && playerViewModel.isFromHomeFragment()) {

                    Track selectedTrack = trackList.get(position);

                    if (playerViewModel.getCurrentTrack().getValue() != null &&
                            playerViewModel.getCurrentTrack().getValue().getId().equals(selectedTrack.getId())) {
                        // Текущий трек уже играет, ничего не делаем
                    } else {
                        // Запускаем воспроизведение трека на новой странице
                        playTrack(selectedTrack);
                    }
                }
            }
        });

        fetchTracks();

        return view;
    }

    private void fetchTracks() {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String url = "http://192.168.100.29:3000/tracks/";
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
        if (playerViewModel.isPlaying().getValue() != null && playerViewModel.isPlaying().getValue()
                && playerViewModel.getCurrentTrack().getValue() != null
                && playerViewModel.getCurrentTrack().getValue().getId().equals(track.getId())) {
            // Трек уже играет, ставим на паузу
            playerViewModel.pauseTrack();
        } else {
            // Запускаем воспроизведение нового трека
            playTrack(track);
        }
    }

    private void playTrack(Track track) {
        String trackUrl = getTrackStreamUrl(track.getId());
        playerViewModel.playTrack(trackUrl, track, true); // true, так как это из HomeFragment
    }

    private void onPlayButtonClicked(Track track) {
        isPlayingStarted = true;
        playTrack(track);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (playerViewModel.isFromHomeFragment()) {
            playerViewModel.stopTrack();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private String getTrackStreamUrl(String trackId) {
        return "http://192.168.100.29:3000/tracks/" + trackId + "/stream";
    }
}
