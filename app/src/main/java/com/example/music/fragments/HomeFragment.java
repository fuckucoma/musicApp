package com.example.music.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.music.view_model.FeedPlayerViewModel;
import com.example.music.view_model.HomeViewModel;
import com.example.music.repository.FavoriteRepository;
import com.example.music.activity.MainActivity;
import com.example.music.view_model.PlayerViewModel;
import com.example.music.adapters.HomeAdapter;
import com.example.music.models.Track;
import com.example.test.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FeedPlayerViewModel feedPlayerViewModel;  // Вместо playerViewModel
    private ViewPager2 viewPager;
    private HomeAdapter homeAdapter;
    private List<Track> trackList = new ArrayList<>();
    private PlayerViewModel playerViewModel;
    private HomeViewModel homeViewModel;
    private FavoriteRepository favoriteRepository;
    private boolean isFetchingTracks = false;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        feedPlayerViewModel = new ViewModelProvider(requireActivity()).get(FeedPlayerViewModel.class);
        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        favoriteRepository = ((MainActivity) requireActivity()).getFavoriteRepository();

        viewPager = view.findViewById(R.id.view_pager);
        homeAdapter = new HomeAdapter(getContext(), trackList, favoriteRepository,
                this::onItemClicked, this::onFavoriteClicked,feedPlayerViewModel);

        viewPager.setAdapter(homeAdapter);

        // При скролле ViewPager2 - когда страница выбирается:
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                Log.d("HomeFragment", "Page selected: " + position);
                // Если feedPlayerViewModel уже что-то играет:
                if (feedPlayerViewModel.isHomePlaying()) {
                    Track selectedTrack = trackList.get(position);
                    Log.d("HomeFragment", "Selected track: " + selectedTrack.getTitle());

                    Track currentHomeTrack = feedPlayerViewModel.getCurrentHomeTrack();
                    if (currentHomeTrack != null && currentHomeTrack.getId() == selectedTrack.getId()) {
                        Log.d("HomeFragment", "Current feed track is already playing");
                        playFeedTrack(selectedTrack);
                    } else {
                        Log.d("HomeFragment", "Playing new feed track");
                        playFeedTrack(selectedTrack);
                    }
                }
            }
        });



        // Обновление избранных:
        favoriteRepository.getFavoriteTrackIds().observe(getViewLifecycleOwner(), favoriteIds -> {
            homeAdapter.updateFavoriteState();
        });

        homeViewModel.getFeedTracks().observe(getViewLifecycleOwner(), tracks -> {
            trackList.clear();
            trackList.addAll(tracks);
            homeAdapter.notifyDataSetChanged();
        });

        feedPlayerViewModel.isFeedPlaying().observe(getViewLifecycleOwner(), isPlaying -> {
            homeAdapter.notifyDataSetChanged();
        });
        feedPlayerViewModel.getFeedCurrentTrack().observe(getViewLifecycleOwner(), track -> {
            homeAdapter.notifyDataSetChanged();
        });

        homeViewModel.loadFeedTracks();
        return view;
    }

    private void onFavoriteClicked(Track track, int position) {
        boolean isFavorite = favoriteRepository.isTrackFavorite(track.getId());
        if (isFavorite) {
            favoriteRepository.removeTrackFromFavorites(track);
//            Snackbar.make(requireView(),"trrt", Snackbar.LENGTH_SHORT).show();
            Toast.makeText(getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            favoriteRepository.addTrackToFavorites(track);
            Toast.makeText(getContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
        }
        homeAdapter.notifyItemChanged(position);
    }

    public void onItemClicked(Track track) {
        if (feedPlayerViewModel.isHomePlaying()) {
            feedPlayerViewModel.pauseFeedTrack();
        } else {
            playFeedTrack(track);
        }
    }

    private void playFeedTrack(Track track) {
        playerViewModel.pauseTrack();
        feedPlayerViewModel.playFeedTrack(track);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (feedPlayerViewModel.isHomePlaying()) {
            feedPlayerViewModel.pauseFeedTrack();
        }
    }
}
