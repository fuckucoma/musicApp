package com.example.music.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.music.models.Track;
import com.example.test.R;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.TrackViewHolder> {

    private Context context;
    private List<Track> trackList;
    private OnTrackSelectedListener listener;

    public interface OnTrackSelectedListener {
        void onTrackSelected(Track track);
    }

    public SearchAdapter(Context context, List<Track> trackList, OnTrackSelectedListener listener) {
        this.context = context;
        this.trackList = trackList;
        this.listener = listener;
    }

    public void updateData(List<Track> newTracks) {
        trackList.clear();
        trackList.addAll(newTracks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_track_search, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = trackList.get(position);

        holder.trackTitle.setText(track.getTitle());
        holder.trackArtist.setText(track.getArtist());

        if (track.getImageUrl() != null && !track.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(track.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.trackImage);
        } else {
            holder.trackImage.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemView.setOnClickListener(v -> listener.onTrackSelected(track));
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView trackTitle;
        TextView trackArtist;
        ImageView trackImage;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackTitle = itemView.findViewById(R.id.track_title);
            trackArtist = itemView.findViewById(R.id.track_artist);
            trackImage = itemView.findViewById(R.id.track_image);
        }
    }
}
