package com.example.music.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.music.UploadTrackActivity;
import com.example.test.R;

public class LibraryFragment extends Fragment {

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.library_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Button upload_btn = view.findViewById(R.id.upload_track);

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), UploadTrackActivity.class);
                startActivity(intent);
            }
        });


        return view;
    }
}