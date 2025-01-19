package com.example.music.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music.models.Track;
import com.example.test.R;

import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private List<Track> tracks;
    private Context context;

    public TrackAdapter(Context context,List<Track> tracks) {
        this.tracks = tracks;
        this.context = context;
    }

    public void updateData(List<Track> newTracks) {
        tracks.clear();
        tracks.addAll(newTracks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_tracks_item, parent, false); // Предположим, что ваш элемент называется item_track.xml
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = tracks.get(position);
        holder.trackTitle.setText(track.getTitle());
        holder.artistName.setText(track.getArtist());

        // Загрузка изображения с использованием Glide
        if (track.getImageUrl() != null && !track.getImageUrl().isEmpty()) {
            Glide.with(holder.trackImage.getContext())
                    .load(track.getImageUrl())
                    .placeholder(R.drawable.placeholder_image) // Замените на ваш ресурс по умолчанию
                    .into(holder.trackImage);
        } else {
            holder.trackImage.setImageResource(R.drawable.placeholder_image); // Замените на ваш ресурс по умолчанию
        }

//        holder.deleteButton.setOnClickListener(v -> {
//            if (deleteListener != null) {
//                deleteListener.onDelete(Integer.parseInt(track.getId()+""));
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        ImageView trackImage;
        TextView trackTitle;
        TextView artistName;
        Button deleteButton;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackImage = itemView.findViewById(R.id.track_image);
            trackTitle = itemView.findViewById(R.id.track_title);
            artistName = itemView.findViewById(R.id.artist_name);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

}
