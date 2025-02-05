package com.example.music.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.music.adapters.HistoryAdapter;
import com.example.music.models.Track;
import com.example.music.view_model.PlayerViewModel;
import com.example.test.R;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private PlayerViewModel playerViewModel;
    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private List<Track> historyList = new ArrayList<>();
    private ImageView btn_back_history;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Инициализируем RecyclerView
        recyclerView = view.findViewById(R.id.history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btn_back_history = view.findViewById(R.id.btn_back_history);

        // Адаптер для списка треков
        historyAdapter = new HistoryAdapter(getContext(), historyList);
        recyclerView.setAdapter(historyAdapter);

        // Инициализируем HistoryViewModel
        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);

        // Наблюдаем за изменениями в истории
        playerViewModel.getListeningHistory().observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null) {
                historyList.clear();
                historyList.addAll(tracks);
                historyAdapter.notifyDataSetChanged();
            }
        });

        btn_back_history.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(HistoryFragment.this);
            navController.popBackStack();
        });

        return view;
    }
}