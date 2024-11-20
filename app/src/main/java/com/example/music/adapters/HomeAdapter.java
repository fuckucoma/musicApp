package com.example.music.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.music.repository.FavoriteRepository;
import com.example.music.models.Track;
import com.example.test.R;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.TrackViewHolder> {

    private Context context;
    private List<Track> trackList;
    private OnItemClickListener listener;
    private OnFavoriteClickListener favoriteClickListener;
    private FavoriteRepository favoriteRepository;


    public interface OnItemClickListener {
        void onItemClicked(Track track);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClicked(Track track, boolean isFavorite);
    }

    public HomeAdapter(Context context, List<Track> trackList, FavoriteRepository favoriteRepository,OnItemClickListener listener ,OnFavoriteClickListener favoriteClickListener) {
        this.context = context;
        this.trackList = trackList;
        this.listener = listener;
        this.favoriteRepository = favoriteRepository;
        this.favoriteClickListener = favoriteClickListener;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_track, parent, false);
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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClicked(track);
            }
        });



        boolean isFavorite = favoriteRepository.isTrackFavorite(track.getId());
        holder.favoriteButton.setImageResource(isFavorite ? R.drawable.ic_heart__24 : R.drawable.ic_favorite_24px);

        holder.favoriteButton.setOnClickListener(v -> {
            favoriteClickListener.onFavoriteClicked(track, isFavorite);
            notifyItemChanged(position);
        });

    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView trackTitle;
        TextView trackArtist;
        ImageView trackImage;
        ImageButton favoriteButton;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackTitle = itemView.findViewById(R.id.track_title);
            trackArtist = itemView.findViewById(R.id.track_artist);
            trackImage = itemView.findViewById(R.id.track_image);
            favoriteButton = itemView.findViewById(R.id.btn_favorite);
        }
    }
}

