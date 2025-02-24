package com.example.music.AdminPanel;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.music.adapters.TrackAdapter;
import com.example.music.models.Track;
import com.example.music.repository.AdminRepository;
import com.example.music.view_model.AdminViewModel;
import com.example.music.view_model.PlayerViewModel;
import com.example.test.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackManagmentFragment extends Fragment {

    private RecyclerView tracksRecyclerView;
    private PlayerViewModel playerViewModel;
    private AdminViewModel adminViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_managment, container, false);
        tracksRecyclerView = view.findViewById(R.id.tracks_recycler_view);
        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        adminViewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        adminViewModel.getTracksLiveData().observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null) {
                TrackAdapter adapter = new TrackAdapter(requireContext(), tracks, trackId -> deleteTrack(trackId));
                tracksRecyclerView.setAdapter(adapter);
            } else {
                // Ошибка или пустой список
            }
        });

        // Первичная загрузка треков
        adminViewModel.fetchTracks();

        return view;
    }



//    private void fetchTracks() {
//        AdminRepository.getInstance().fetchTracks(new AdminRepository.MyCallback<List<Track>>() {
//            @Override
//            public void onSuccess(List<Track> data) {
//                playerViewModel.getTracks().observe(getViewLifecycleOwner(), tracks ->{
//                    TrackAdapter adapter = new TrackAdapter(requireContext(), data, trackId -> deleteTrack(trackId));
//                    tracksRecyclerView.setAdapter(adapter);
//                });
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                if (!isAdded()) return;
//                Log.e("TrackManagementFragment", "Ошибка загрузки треков: " + t.getMessage());
//            }
//        });
//    }

    private void deleteTrack(int trackId) {
        adminViewModel.deleteTracks(trackId);
    }

}
