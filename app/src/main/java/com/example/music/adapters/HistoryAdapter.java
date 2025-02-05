package com.example.music.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music.models.Track;
import com.example.test.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<Track> trackList;

    public HistoryAdapter(Context context, List<Track> trackList) {
        this.context = context;
        this.trackList = trackList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_track, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Track track = trackList.get(position);
        holder.tvTitle.setText(track.getTitle());
        holder.tvArtist.setText(track.getArtist());


         Glide.with(context).load(track.getImageUrl()).into(holder.ivArtwork);
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist;
        ImageView ivArtwork;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.history_track_title);
            tvArtist = itemView.findViewById(R.id.history_track_artist);
            ivArtwork = itemView.findViewById(R.id.history_track_image);
        }
    }
}
