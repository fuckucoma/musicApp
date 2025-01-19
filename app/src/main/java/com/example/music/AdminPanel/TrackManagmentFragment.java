package com.example.music.AdminPanel;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.music.adapters.TrackAdapter;
import com.example.music.models.Track;
import com.example.music.repository.AdminRepository;
import com.example.test.R;

import java.util.List;

public class TrackManagmentFragment extends Fragment {

    private RecyclerView tracksRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_managment, container, false);
        tracksRecyclerView = view.findViewById(R.id.tracks_recycler_view);
        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchTracks();
        return view;
    }

    private void fetchTracks() {
        AdminRepository.getInstance().fetchTracks(new AdminRepository.MyCallback<List<Track>>() {
            @Override
            public void onSuccess(List<Track> data) {
                TrackAdapter adapter = new TrackAdapter(requireContext(), data);
                tracksRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onError(Throwable t) {
                Log.e("TrackManagementFragment", "Ошибка загрузки треков: " + t.getMessage());
            }
        });
    }
}
