package com.example.music.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.music.view_model.FeedPlayerViewModel;
import com.example.music.PlaybackSource;
import com.example.music.view_model.PlayerViewModel;
import com.example.music.adapters.SearchAdapter;
import com.example.music.models.Track;
import com.example.music.view_model.ProfileViewModel;
import com.example.music.view_model.SearchViewModel;
import com.example.test.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.textfield.TextInputEditText;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {

    private PlayerViewModel playerViewModel;
    private SearchViewModel searchViewModel;
    private SearchAdapter searchAdapter;
    private RecyclerView recyclerView;
    private FeedPlayerViewModel feedPlayerViewModel;
    private EditText searchInput;
    private MaterialButton searchButton;
    private ImageView search_icon, profile;
    private ProfileViewModel profileViewModel;

    private List<Track> currentSearchTracks = new ArrayList<>();

    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        feedPlayerViewModel = new ViewModelProvider(requireActivity()).get(FeedPlayerViewModel.class);
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        recyclerView = view.findViewById(R.id.search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchInput = view.findViewById(R.id.search_input);
        profile = view.findViewById(R.id.profile_image);

        searchAdapter = new SearchAdapter(getContext(), new ArrayList<>(), track -> {
            feedPlayerViewModel.pauseFeedTrack();
            PlaybackSource source = PlaybackSource.SEARCH;
            playerViewModel.playTrack(track, source);

            searchViewModel.addToSearchHistory(track);
        });
        recyclerView.setAdapter(searchAdapter);

        searchAdapter.updateData(searchViewModel.getSearchHistory().getValue());

        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {

                String imageUrl = userProfile.getProfileImageUrl();
                if (!TextUtils.isEmpty(imageUrl)) {
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_profile_24)
                            .into(profile);
                } else {
                    profile.setImageResource(R.drawable.ic_profile_24);
                }
            } else {
                profile.setImageResource(R.drawable.ic_profile_24);
            }
        });

        profile.setOnClickListener(v -> {
            DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawer_layout);
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Можно использовать для каких-то действий перед изменением текста

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Выполняем поиск, если текст изменился
                if (charSequence.length() > 0) {
                    searchViewModel.searchTracks(charSequence.toString());  // Здесь вызываешь метод поиска

                    searchViewModel.getSearchTracks().observe(getViewLifecycleOwner(), tracks -> {
                        if (tracks != null) {
                            currentSearchTracks.clear();
                            currentSearchTracks.addAll(tracks);
                            searchAdapter.updateData(tracks);
                        } else {
                            Toast.makeText(getContext(), "Ошибка поиска треков", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    searchAdapter.updateData(new ArrayList<>());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        searchInput.setText("");

        searchAdapter.updateData(searchViewModel.getSearchHistory().getValue());

    }
}
