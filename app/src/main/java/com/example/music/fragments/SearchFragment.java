package com.example.music.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.music.PlayerViewModel;
import com.example.music.adapters.SearchAdapter;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.Track;
import com.example.music.SearchViewModel;
import com.example.test.BuildConfig;
import com.example.test.R;
import com.google.android.material.button.MaterialButton;

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
    private EditText searchInput;
    private MaterialButton searchButton;
    private ApiService apiService;

    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);


        recyclerView = view.findViewById(R.id.search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchInput = view.findViewById(R.id.search_input);
        searchButton = view.findViewById(R.id.search_button);

        searchAdapter = new SearchAdapter(getContext(), new ArrayList<>(), this::onTrackSelected);
        recyclerView.setAdapter(searchAdapter);

        apiService = ApiClient.getClient().create(ApiService.class);


        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString();
            if (!query.isEmpty()) {
                searchViewModel.searchTracks(query);
            }
        });

        // Наблюдаем за результатами поиска
        searchViewModel.getSearchResults().observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null) {
                searchAdapter.updateData(tracks);
            } else {
                Toast.makeText(getContext(), "Ошибка поиска треков", Toast.LENGTH_SHORT).show();
            }
        });

        // Если были предыдущие результаты поиска, отображаем их
        if (searchViewModel.getSearchResults().getValue() != null && !searchViewModel.getSearchResults().getValue().isEmpty()) {
            searchAdapter.updateData(searchViewModel.getSearchResults().getValue());
            searchInput.setText(searchViewModel.getLastQuery());
        }

        return view;
    }

//    private void searchTracks(String query) {
//        ApiService apiService = ApiClient.getClient().create(ApiService.class);
//        Call<List<Track>> call = apiService.searchTracks(query);
//        call.enqueue(new Callback<List<Track>>() {
//            @Override
//            public void onResponse(Call<List<Track>> call, Response<List<Track>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<Track> searchResults = response.body();
//
//                    searchViewModel.setSearchResults(searchResults);
//                } else {
//                    Toast.makeText(getContext(), "Ошибка поиска треков", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Track>> call, Throwable t) {
//                Log.e("SearchTracks", "Ошибка сети", t);
//                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void onTrackSelected(Track track) {
        String trackUrl = getTrackStreamUrl(track.getId()+"");
        playerViewModel.playTrack(trackUrl, track);
        Log.d("PlayerFragment", "Track selected: " + track.getTitle());
    }

    private String getTrackStreamUrl(String trackId) {
        return BuildConfig.BASE_URL + "/tracks/" + trackId + "/stream";
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
