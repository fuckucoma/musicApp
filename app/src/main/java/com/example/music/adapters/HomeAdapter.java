package com.example.music.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.music.repository.FavoriteRepository;
import com.example.music.models.Track;
import com.example.music.view_model.FeedPlayerViewModel;
import com.example.test.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicatorSpec;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.TrackViewHolder> {

    private Context context;
    private List<Track> trackList;
    private OnItemClickListener listener;
    private OnFavoriteClickListener favoriteClickListener;
    private FavoriteRepository favoriteRepository;
    private FeedPlayerViewModel feedPlayerViewModel;

    public interface OnItemClickListener {
        void onItemClicked(Track track);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClicked(Track track, int position);
    }

    public HomeAdapter(Context context,
                       List<Track> trackList,
                       FavoriteRepository favoriteRepository,
                       OnItemClickListener listener,
                       OnFavoriteClickListener favoriteClickListener,
                       FeedPlayerViewModel feedPlayerViewModel)
    {
        this.context = context;
        this.trackList = trackList;
        this.listener = listener;
        this.favoriteRepository = favoriteRepository;
        this.favoriteClickListener = favoriteClickListener;
        this.feedPlayerViewModel = feedPlayerViewModel;
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
            Glide.with(context)
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


        Track currentTrack = feedPlayerViewModel.getFeedCurrentTrack().getValue();
        Boolean isPlaying = feedPlayerViewModel.isFeedPlaying().getValue();
        boolean isFeedPlaying = (isPlaying != null && isPlaying);

        // 2. Проверяем, совпадает ли track с currentTrack и играет ли
        if (currentTrack != null && currentTrack.getId() == track.getId() && isFeedPlaying) {
            // Показываем анимацию
            holder.equalizerAnimation.setVisibility(View.VISIBLE);
            if (!holder.equalizerAnimation.isAnimating()) {
                holder.equalizerAnimation.playAnimation();
            }
        } else {
            // Иначе прячем и останавливаем анимацию
            holder.equalizerAnimation.cancelAnimation();
            holder.equalizerAnimation.setVisibility(View.GONE);
        }

        holder.favoriteButton.setOnClickListener(v -> {
            favoriteClickListener.onFavoriteClicked(track, position);
        });
    }

    public void updateFavoriteState() {
        for (int i = 0; i < trackList.size(); i++) {
            notifyItemChanged(i);
        }
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
        LottieAnimationView equalizerAnimation;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackTitle = itemView.findViewById(R.id.track_title);
            trackArtist = itemView.findViewById(R.id.track_artist);
            trackImage = itemView.findViewById(R.id.track_image);
            favoriteButton = itemView.findViewById(R.id.btn_favorite);
            equalizerAnimation = itemView.findViewById(R.id.equalizerAnimation);
        }
    }
}

