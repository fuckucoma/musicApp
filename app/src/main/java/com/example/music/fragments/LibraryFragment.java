package com.example.music.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.music.FavoriteRequest;
import com.example.music.PlayerViewModel;
import com.example.music.UploadTrackActivity;
import com.example.music.adapters.LibraryAdapter;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.FavoriteResponse;
import com.example.music.models.Track;
import com.example.test.BuildConfig;
import com.example.test.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LibraryFragment extends Fragment {

    private static final String TAG = "LibraryFragment";

    private PlayerViewModel playerViewModel;
    private ApiService apiService;

    private Button btnUploadTrack;
    private ImageView ivProfile;

    private RecyclerView recyclerView;
    private LibraryAdapter libraryAdapter;
    private List<Track> trackList = new ArrayList<>();
    private ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);


        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);

        // Инициализация RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.library_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        apiService = ApiClient.getClient().create(ApiService.class);

        // Инициализация trackList и libraryAdapter
        trackList = new ArrayList<>();
        libraryAdapter = new LibraryAdapter(getContext(), trackList, this::onTrackSelected, this::removeTrackFromFavorites);


        recyclerView.setAdapter(libraryAdapter);

        // Инициализация кнопок
        btnUploadTrack = view.findViewById(R.id.upload_track);
        ivProfile = view.findViewById(R.id.ivProfile);

        // Инициализация progressBar
        progressBar = view.findViewById(R.id.progress_bar);



        btnUploadTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), UploadTrackActivity.class);
                startActivity(intent);
            }
        });

        ivProfile.setOnClickListener(v -> openUserProfile());

        getFavorites();

        return view;
    }



    private void removeTrackFromFavorites(Track track, boolean isFavorite) {
        // Здесь можно игнорировать значение isFavorite, если оно не нужно
        FavoriteRequest favoriteRequest = new FavoriteRequest(track.getId());
        apiService.removeFavorite(favoriteRequest).enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Трек удален из избранного", Toast.LENGTH_SHORT).show();
                   getFavorites();
                } else {
                    Toast.makeText(getContext(), "Ошибка при удалении из избранного", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFavorites() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getFavorites().enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    trackList.clear();

                    for (FavoriteResponse.FavoriteTrack favoriteTrack : response.body().getFavoriteTracks()) {
                        Track track = new Track();
                        track.setId(favoriteTrack.getTrackId());
                        track.setTitle(favoriteTrack.getTitle());
                        track.setArtist(favoriteTrack.getArtist());
                        track.setImageUrl(favoriteTrack.getImageUrl());
                        track.setFilename(favoriteTrack.getFilename());
                        trackList.add(track);
                    }

                    libraryAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Не удалось получить избранное", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getContext(), "Ошибка при получении избранного", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void openUserProfile() {
        Log.d(TAG, "Opening UserFragment");
        UserFragment userFragment = new UserFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, userFragment); // Убедитесь, что у вас есть контейнер с id 'fragment_container' в вашем Activity
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    private void onTrackSelected(Track track) {
        String trackUrl = getTrackStreamUrl(track.getId()+"");

        Log.d(TAG, "Playing track URL: " + trackUrl);
        playerViewModel.playTrack(trackUrl,track);
    }

    private String getTrackStreamUrl(String trackId) {
        return BuildConfig.BASE_URL +"/tracks/" + trackId + "/stream";
    }
}