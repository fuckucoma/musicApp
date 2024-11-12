package com.example.music.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;


import com.example.music.LoginActivity;

import com.example.music.PlayerViewModel;
import com.example.music.UploadTrackActivity;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.Track;
import com.example.test.R;


public class LibraryFragment extends Fragment {

    private static final String TAG = "LibraryFragment";

    private PlayerViewModel playerViewModel;

    private Button btnUploadTrack;
    private ImageView ivProfile;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.library_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnUploadTrack = view.findViewById(R.id.upload_track);

        ivProfile=view.findViewById(R.id.ivProfile);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);



        btnUploadTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), UploadTrackActivity.class);
                startActivity(intent);
            }
        });


        ivProfile.setOnClickListener(v -> openUserProfile());

        return view;
    }


    private void openUserProfile() {
        Log.d(TAG, "Opening UserFragment");

        // Создаём экземпляр UserFragment
        UserFragment userFragment = new UserFragment();

        // Получаем FragmentManager и начинаем транзакцию
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        // Заменяем текущий фрагмент на UserFragment
        transaction.replace(R.id.fragment_container, userFragment); // Убедитесь, что у вас есть контейнер с id 'fragment_container' в вашем Activity

        // Добавляем транзакцию в back stack, чтобы пользователь мог вернуться
        transaction.addToBackStack(null);

        // Выполняем транзакцию
        transaction.commit();
    }


    private void onTrackSelected(Track track) {
        String trackUrl = getTrackStreamUrl(track.getId());
        playerViewModel.playTrack(trackUrl, track); // false, так как не из HomeFragment
    }

    private String getTrackStreamUrl(String trackId) {
        return "http://192.168.100.4:3000/tracks/" + trackId + "/stream";
    }

    private String getAuthToken() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("authToken", null);
    }

}