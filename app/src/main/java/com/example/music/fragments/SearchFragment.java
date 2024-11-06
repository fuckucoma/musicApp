package com.example.music.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.music.adapters.SearchAdapter;
import com.example.music.models.Track;
import com.example.music.PlayerViewModel;
import com.example.test.R;
import com.google.gson.Gson;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchFragment extends Fragment {

    private PlayerViewModel playerViewModel; // ViewModel для управления плеером
    private SearchAdapter searchAdapter;
    private final List<Track> trackList = new ArrayList<>();
    private RecyclerView recyclerView;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Получаем экземпляр PlayerViewModel
        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);

        recyclerView = view.findViewById(R.id.search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        EditText searchInput = view.findViewById(R.id.search_input);
        Button searchButton = view.findViewById(R.id.search_button);

        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString();
            if (!query.isEmpty()) {
                searchTracks(query);  // Выполняем поиск с запросом
            }
        });

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void searchTracks(String query) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String url = "http://192.168.100.30:3000/search-tracks?query=" + query;

            Log.d("SearchTracks", "URL запроса: " + url);

            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                Log.d("SearchTracks", "Код ответа: " + response.code());
                Log.d("SearchTracks", "Тело ответа: " + responseBody);

                if (response.isSuccessful()) {
                    List<Track> searchResults = new Gson().fromJson(responseBody, new TypeToken<List<Track>>(){}.getType());
                    requireActivity().runOnUiThread(() -> {
                        searchAdapter = new SearchAdapter(getContext(), trackList, track -> {
                            playTrack(track.getId());
                        }, playerViewModel.getPlayer());
                        recyclerView.setAdapter(searchAdapter);

                        trackList.clear();
                        trackList.addAll(searchResults);
                        searchAdapter.notifyDataSetChanged();
                    });
                } else {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Ошибка поиска треков", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                Log.e("SearchTracks", "Ошибка сети", e);
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Ошибка сети: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void playTrack(String trackId) {
        String trackUrl = getTrackStreamUrl(trackId);
        Log.d("SearchFragment", "playTrack: " + trackUrl);

        if (trackUrl == null || trackUrl.isEmpty()) {
            Toast.makeText(getContext(), "Ошибка: некорректный URL трека", Toast.LENGTH_SHORT).show();
            return;
        }

        // Используем ViewModel для управления воспроизведением
        playerViewModel.playTrack(trackUrl);
    }

    private String getTrackStreamUrl(String trackId) {
        return "http://192.168.100.30:3000/tracks/" + trackId + "/stream";
    }
}
