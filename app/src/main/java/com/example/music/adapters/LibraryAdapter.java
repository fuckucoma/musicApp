package com.example.music.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
import com.google.android.exoplayer2.ExoPlayer;
import com.squareup.picasso.Picasso;

import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.TrackViewHolder> {

    private final Context context;
    private List<Track> trackList;
    private final OnTrackClickListener listener;
    private final OnFavoriteClickListener favoriteListener;

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

        String imageUrl = track.getImageUrl();

        Log.d("Favorite","Image favorite track: "+ imageUrl);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context).load(track.getImageUrl()).into(holder.trackImage);
        } else {
            holder.trackImage.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemView.setOnClickListener(v -> listener.onTrackClick(track));


        holder.favoriteIcon.setOnClickListener(v -> {
            boolean isFavorite = true;
            favoriteListener.onFavoriteClick(track, isFavorite);
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
        ImageButton favoriteIcon;

        @SuppressLint("WrongViewCast")
        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackImage = itemView.findViewById(R.id.track_image);
            trackTitle = itemView.findViewById(R.id.track_title);
            trackName = itemView.findViewById(R.id.track_artist);
            favoriteIcon = itemView.findViewById(R.id.favorite_button);
        }
    }
}
