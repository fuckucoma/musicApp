package com.example.music.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.music.PlayerViewModel;
import com.example.test.R;
import com.example.music.adapters.HomeAdapter;
import com.example.music.models.Track;
import com.example.music.models.TrackResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private PlayerViewModel playerViewModel; // ViewModel для управления плеером
    private ViewPager2 viewPager;
    private HomeAdapter trackAdapter;
    private final List<Track> trackList = new ArrayList<>();
    private int playingPosition = -1;

    @SuppressLint({"MissingInflatedId", "NonConstantResourceId"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Получаем экземпляр PlayerViewModel
        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);

        viewPager = view.findViewById(R.id.view_pager);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Track selectedTrack = trackList.get(position);
                playTrack(selectedTrack.getId()); // Запуск воспроизведения при смене страницы
                playingPosition = position;
            }
        });

        fetchTracks();

        return view;
    }

    private void fetchTracks() {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String url = "http://192.168.100.30:3000/tracks/";
            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                Log.d("ApiService", "Response Code: " + response.code());
                Log.d("ApiService", "Response Body: " + responseBody);

                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    TrackResponse.TrackData[] trackArray = gson.fromJson(responseBody, TrackResponse.TrackData[].class);

                    if (isAdded()) { // Проверяем, прикреплен ли фрагмент
                        requireActivity().runOnUiThread(() -> {
                            trackList.clear(); // Очищаем список, чтобы избежать дублирования
                            for (TrackResponse.TrackData data : trackArray) {
                                trackList.add(new Track(data.getId(), data.getTitle(), data.getArtist(), data.getAlbum(), data.getImageUrl(), data.getFilename(), data.getCreatedAt()));
                            }

                            // Инициализируем адаптер после заполнения списка
                            trackAdapter = new HomeAdapter(getContext(), trackList, track -> {
                                playTrack(track.getId());
                            }, playerViewModel.getPlayer()); // Используем плеер из ViewModel
                            viewPager.setAdapter(trackAdapter);
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

    private void playTrack(String trackId) {
        String trackUrl = getTrackStreamUrl(trackId); // Получаем URL трека
        Log.d("HomeFragment", "playTrack: " + trackUrl);

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
