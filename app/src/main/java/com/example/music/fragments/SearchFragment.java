package com.example.music.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.music.view_model.FeedPlayerViewModel;
import com.example.music.PlaybackSource;
import com.example.music.view_model.PlayerViewModel;
import com.example.music.adapters.SearchAdapter;
import com.example.music.models.Track;
import com.example.music.view_model.SearchViewModel;
import com.example.test.R;
import com.google.android.material.button.MaterialButton;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {

    private PlayerViewModel playerViewModel;
    private SearchViewModel searchViewModel;
    private SearchAdapter searchAdapter;
    private RecyclerView recyclerView;
    private FeedPlayerViewModel feedPlayerViewModel;
    private EditText searchInput;
    private MaterialButton searchButton;
    private ImageView search_icon;

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
        search_icon = view.findViewById(R.id.search_icon);

        searchAdapter = new SearchAdapter(getContext(), new ArrayList<>(), track -> {
            feedPlayerViewModel.pauseFeedTrack();
            PlaybackSource source = PlaybackSource.SEARCH;
            playerViewModel.playTrack(track, source);

        });
        recyclerView.setAdapter(searchAdapter);

        search_icon.setOnClickListener(v -> {
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
