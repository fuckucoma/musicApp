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
import com.example.music.models.Track;
import com.example.music.SearchViewModel;
import com.example.test.R;
import com.google.android.material.button.MaterialButton;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchFragment extends Fragment {

    private PlayerViewModel playerViewModel;
    private SearchViewModel searchViewModel;
    private SearchAdapter searchAdapter;
    private RecyclerView recyclerView;
    private EditText searchInput;
    private MaterialButton searchButton;

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

        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString();
            if (!query.isEmpty()) {
                searchViewModel.setLastQuery(query);
                searchTracks(query);
            }
        });

        // Наблюдаем за результатами поиска
        searchViewModel.getSearchResults().observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null) {
                searchAdapter.updateData(tracks);
            }
        });

        // Если были предыдущие результаты поиска, отображаем их
        if (searchViewModel.getSearchResults().getValue() != null && !searchViewModel.getSearchResults().getValue().isEmpty()) {
            searchAdapter.updateData(searchViewModel.getSearchResults().getValue());
            searchInput.setText(searchViewModel.getLastQuery());
        }

        return view;
    }

    private void searchTracks(String query) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String url = "http://192.168.100.29:3000/search-tracks?query=" + query;

            Log.d("SearchTracks", "URL запроса: " + url);

            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                Log.d("SearchTracks", "Код ответа: " + response.code());
                Log.d("SearchTracks", "Тело ответа: " + responseBody);

                if (response.isSuccessful()) {
                    List<Track> searchResults = new Gson().fromJson(responseBody, new TypeToken<List<Track>>(){}.getType());
                    // Сохраняем результаты в ViewModel
                    searchViewModel.setSearchResults(searchResults);
                } else {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Ошибка поиска треков", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                Log.e("SearchTracks", "Ошибка сети", e);
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Ошибка сети: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void onTrackSelected(Track track) {
        String trackUrl = getTrackStreamUrl(track.getId());
        playerViewModel.playTrack(trackUrl, track); // false, так как не из HomeFragment
    }

    private String getTrackStreamUrl(String trackId) {
        return "http://192.168.100.29:3000/tracks/" + trackId + "/stream";
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
