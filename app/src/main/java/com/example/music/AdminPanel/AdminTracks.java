package com.example.music.AdminPanel;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.music.adapters.TrackAdapter;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.Track;
import com.example.test.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminTracks extends Fragment {

    private RecyclerView recyclerView;
    private TrackAdapter adapter;
    private List<Track> tracks = new ArrayList<>();
    private ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_tracks, container, false);

        recyclerView = view.findViewById(R.id.tracks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        apiService = ApiClient.getClient().create(ApiService.class);

//        adapter = new TrackAdapter(tracks, this::deleteTrack);
//        recyclerView.setAdapter(adapter);

//        fetchTracks();
        return view;
    }

    private void fetchTracks(){
        Call<List<Track>> call = apiService.getAllTracks();
        call.enqueue(new Callback<List<Track>>() {
            @Override
            public void onResponse(@NonNull Call<List<Track>> call, @NonNull Response<List<Track>> response) {
                if(response.isSuccessful() && response.body() != null){
                    List<Track> trackList = response.body();
                    adapter.updateData(trackList);
                }
                else{
                    Log.e("AdminTracks", "Ошибка получения треков: " + response.code());
                    Toast.makeText(getContext(), "Ошибка получения треков", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Track>> call, @NonNull Throwable t) {
                Log.e("AdminTracks", "Ошибка загрузки треков: " + t.getMessage());
                Toast.makeText(getContext(), "Ошибка загрузки треков: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteTrack(int id){
        Call<Void> call = apiService.deleteTrack(id);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()){
                    Toast.makeText(getContext(), "Трек удалён", Toast.LENGTH_SHORT).show();
                    fetchTracks();
                }
                else{
                    Toast.makeText(getContext(), "Ошибка удаления трека", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка удаления трека: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}