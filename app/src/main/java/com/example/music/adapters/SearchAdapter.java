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

import com.example.test.R;
import com.example.music.models.Track;
import com.google.android.exoplayer2.ExoPlayer;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.TrackViewHolder> {

    private final Context context;
    private final List<Track> trackList;
    private final ExoPlayer exoPlayer;
    private final OnTrackClickListener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());


    public interface OnTrackClickListener {
        void onTrackClick(Track track);
    }

    public SearchAdapter(Context context, List<Track> trackList, OnTrackClickListener listener, ExoPlayer exoPlayer) {
        this.context = context;
        this.trackList = trackList;
        this.exoPlayer = exoPlayer;
        this.listener = listener;
    }


    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = trackList.get(position);

        holder.trackTitle.setText(track.getTitle());
        holder.trackName.setText(track.getArtist());

        Log.d("PicassoLoader", "Loading artwork from: " + track.getImageUrl());

        if (track.getImageUrl() != null && !track.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(track.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.trackImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("PicassoLoader", "Image loaded successfully");
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("PicassoLoader", "Error loading image", e);
                        }
                    });
        } else {
            holder.trackImage.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemView.setOnClickListener(v -> {
            listener.onTrackClick(track);  // Передаем трек в listener для обработки
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


