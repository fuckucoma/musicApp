package com.example.music.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music.models.Track;
import com.example.test.R;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.TrackViewHolder> {

    private final Context context;
    private List<Track> trackList;
    private final OnTrackClickListener listener;
    private final OnFavoriteClickListener favoriteListener;
    private final Set<Integer> favoriteTrackIds = new HashSet<>(); // Хранит ID избранных треков

    public interface OnTrackClickListener {
        void onTrackClick(Track track);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Track track, boolean isFavorite);
    }

    public LibraryAdapter(Context context, List<Track> trackList, OnTrackClickListener listener, OnFavoriteClickListener favoriteListener) {
        this.context = context;
        this.trackList = trackList;
        this.listener = listener;
        this.favoriteListener = favoriteListener;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = trackList.get(position);

        holder.trackTitle.setText(track.getTitle());
        holder.trackName.setText(track.getArtist());

        if (track.getImageUrl() != null && !track.getImageUrl().isEmpty()) {
            Glide.with(context).load(track.getImageUrl()).into(holder.trackImage);
        } else {
            holder.trackImage.setImageResource(R.drawable.placeholder_image);
        }

        boolean isFavorite = favoriteTrackIds.contains(track.getId());
        holder.favoriteIcon.setImageResource(isFavorite ? R.drawable.ic_heart__24 : R.drawable.ic_favorite_24px);

        holder.itemView.setOnClickListener(v -> listener.onTrackClick(track));

        holder.favoriteIcon.setOnClickListener(v -> {
            favoriteListener.onFavoriteClick(track, isFavorite);
            // Удалите следующие строки:
            // if (isFavorite) {
            //     favoriteTrackIds.remove(track.getId());
            // } else {
            //     favoriteTrackIds.add(track.getId());
            // }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

//    public void updateFavorites(List<Integer> favoriteIds) {
//        Set<Integer> updatedFavorites = new HashSet<>(favoriteIds);
//        for (int i = 0; i < trackList.size(); i++) {
//            Track track = trackList.get(i);
//            boolean isFavorite = favoriteTrackIds.contains(track.getId());
//            boolean shouldBeFavorite = updatedFavorites.contains(track.getId());
//            if (isFavorite != shouldBeFavorite) {
//                if (shouldBeFavorite) {
//                    favoriteTrackIds.add(track.getId());
//                } else {
//                    favoriteTrackIds.remove(track.getId());
//                }
//                notifyItemChanged(i);
//            }
//        }
//    }

    public void updateFavorites(Set<Integer> favoriteIds) {
        Set<Integer> updatedFavorites = new HashSet<>(favoriteIds);
        for (int i = 0; i < trackList.size(); i++) {
            Track track = trackList.get(i);
            boolean isFavorite = favoriteTrackIds.contains(track.getId());
            boolean shouldBeFavorite = updatedFavorites.contains(track.getId());
            if (isFavorite != shouldBeFavorite) {
                if (shouldBeFavorite) {
                    favoriteTrackIds.add(track.getId());
                } else {
                    favoriteTrackIds.remove(track.getId());
                }
                notifyItemChanged(i);
            }
        }
    }

//    public void updateFavorites(Set<Integer> updatedFavorites) {
//        this.favoriteTrackIds.clear();
//        if (updatedFavorites != null) {
//            this.favoriteTrackIds.addAll(updatedFavorites);
//        }
//        notifyDataSetChanged();
//    }

    public void updateData(List<Track> newTracks, Set<Integer> updatedFavorites) {
        this.trackList = newTracks;
        this.favoriteTrackIds.clear();
        if (updatedFavorites != null) {
            this.favoriteTrackIds.addAll(updatedFavorites); // Обновляем избранное
        }

//        // Сортировка треков по дате создания
//        trackList.sort((track1, track2) -> {
//            Date date1 = track1.getCreatedAtDate();
//            Date date2 = track2.getCreatedAtDate();
//
//            if (date1 == null && date2 == null) return 0;
//            if (date1 == null) return 1;
//            if (date2 == null) return -1;
//
//            return date2.compareTo(date1);
//        });

        notifyDataSetChanged();
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        ImageView trackImage;
        TextView trackTitle;
        TextView trackName;
        ImageButton favoriteIcon;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackImage = itemView.findViewById(R.id.track_image);
            trackTitle = itemView.findViewById(R.id.track_title);
            trackName = itemView.findViewById(R.id.track_artist);
            favoriteIcon = itemView.findViewById(R.id.favorite_button);
        }
    }
}
