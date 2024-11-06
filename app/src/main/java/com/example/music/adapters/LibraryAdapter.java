package com.example.music.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.models.Track;
import com.example.test.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.squareup.picasso.Picasso;

import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.TrackViewHolder>{


    private final Context context;
    private final List<Track> trackList;
    private final ExoPlayer exoPlayer;
    private final OnTrackClickListener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());


    public interface OnTrackClickListener {
        void onTrackClick(Track track);
    }

    public LibraryAdapter(Context context, List<Track> trackList, OnTrackClickListener listener, ExoPlayer exoPlayer) {
        this.context = context;
        this.trackList = trackList;
        this.exoPlayer = exoPlayer;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.library_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = trackList.get(position);

        holder.trackTitle.setText(track.getTitle());
        holder.trackName.setText(track.getArtist());

        Log.d("PicassoLoader", "Loading artwork from: " + track.getImageUrl());

        if (track.getImageUrl() != null && !track.getImageUrl().isEmpty()) {
            Picasso.get().load(track.getImageUrl()).into(holder.trackImage);
        } else {
            holder.trackImage.setImageResource(R.drawable.placeholder_image);
        }


        holder.itemView.setOnClickListener(v -> {
            listener.onTrackClick(track);
        });

    }


    @Override
    public int getItemCount() {
        return trackList.size();
    }



    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        ImageView trackImage;
        TextView trackTitle;
        TextView trackName;



        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackImage = itemView.findViewById(R.id.track_image);
            trackTitle = itemView.findViewById(R.id.track_title);
            trackName = itemView.findViewById(R.id.track_name);

        }
    }
}
