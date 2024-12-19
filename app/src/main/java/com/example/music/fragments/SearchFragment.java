package com.example.music.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.music.FeedPlayerViewModel;
import com.example.music.PlaybackSource;
import com.example.music.PlayerViewModel;
import com.example.music.adapters.SearchAdapter;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.Track;
import com.example.music.SearchViewModel;
import com.example.music.service.MusicService;
import com.example.test.BuildConfig;
import com.example.test.R;
import com.google.android.material.button.MaterialButton;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SearchFragment extends Fragment {

    private PlayerViewModel playerViewModel;
    private SearchViewModel searchViewModel;
    private SearchAdapter searchAdapter;
    private RecyclerView recyclerView;
    private FeedPlayerViewModel feedPlayerViewModel;
    private EditText searchInput;
    private MaterialButton searchButton;

    private List<Track> currentSearchTracks = new ArrayList<>();

    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        feedPlayerViewModel = new ViewModelProvider(requireActivity()).get(FeedPlayerViewModel.class);

        recyclerView = view.findViewById(R.id.search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchInput = view.findViewById(R.id.search_input);
        searchButton = view.findViewById(R.id.search_button);

        searchAdapter = new SearchAdapter(getContext(), new ArrayList<>(), track -> {
            String trackUrl = BuildConfig.BASE_URL + "/tracks/" + track.getId() + "/stream";
            feedPlayerViewModel.pauseFeedTrack();
//            playerViewModel.SetCurrentTrack(track);
            //playerViewModel.playTrack(trackUrl, track, PlaybackSource.SEARCH);
            //,PlaybackSource.SEARCH,currentSearchTracks
//            Intent intent = new Intent(getContext(), MusicService.class);
//            intent.setAction("PLAY_TRACK");
//            intent.putExtra("TRACK_ID", track.getId());
//            intent.putExtra("PLAYBACK_SOURCE", PlaybackSource.SEARCH.name()); // Указываем источник
//            ContextCompat.startForegroundService(getContext(), intent);

            PlaybackSource source = PlaybackSource.SEARCH;
            playerViewModel.playTrack(track, source);

        });
        recyclerView.setAdapter(searchAdapter);

        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString();
            if (!query.isEmpty()) {
                searchViewModel.searchTracks(query);
            }
        });

        searchViewModel.getSearchTracks().observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null) {
                currentSearchTracks.clear();
                currentSearchTracks.addAll(tracks);
                searchAdapter.updateData(tracks);
            } else {
                Toast.makeText(getContext(), "Ошибка поиска треков", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
