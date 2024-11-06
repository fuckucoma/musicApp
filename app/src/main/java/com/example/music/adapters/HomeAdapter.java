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
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.models.Track;
import com.example.test.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.TrackViewHolder> {

    private final Context context;
    private final List<Track> trackList;
    private final ExoPlayer exoPlayer;
    private final OnTrackClickListener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());


    public interface OnTrackClickListener {
        void onTrackClick(Track track);
    }

    public HomeAdapter(Context context, List<Track> trackList, OnTrackClickListener listener, ExoPlayer exoPlayer) {
        this.context = context;
        this.trackList = trackList;
        this.exoPlayer = exoPlayer;
        this.listener = listener;
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
        holder.trackDescription.setText(track.getArtist());

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

        // Play/Pause button functionality
        holder.playPauseButton.setOnClickListener(v -> {
            if (exoPlayer.isPlaying()) {
                exoPlayer.pause();
                holder.playPauseButton.setImageResource(R.drawable.ic_play);
            } else {
                //listener.onTrackClick(track);
                if (exoPlayer.getPlaybackState() == Player.STATE_IDLE || exoPlayer.getPlaybackState() == Player.STATE_ENDED) {
                    exoPlayer.prepare();
                }
                exoPlayer.play();
                holder.playPauseButton.setImageResource(R.drawable.ic_pause);
            }
        });

        // Update seekbar with current position
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == ExoPlayer.STATE_READY) {
                    holder.seekBar.setMax((int) exoPlayer.getDuration() / 1000);
                    updateSeekBar(holder);
                }
            }
        });

        // Handle seekbar change
        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    exoPlayer.seekTo(progress * 1000L);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateSeekBar(TrackViewHolder holder) {
        handler.postDelayed(() -> {
            if (exoPlayer.isPlaying()) {
                long currentPosition = exoPlayer.getCurrentPosition() / 1000;
                long totalDuration = exoPlayer.getDuration() / 1000;
                holder.seekBar.setProgress((int) currentPosition);


                holder.currentTime.setText(formatTime(currentPosition));
                holder.totalTime.setText(formatTime(totalDuration));

                updateSeekBar(holder);
            }
        }, 100);
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }


    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        ImageView trackImage;
        TextView trackTitle;
        TextView trackDescription;
        SeekBar seekBar;
        ImageButton playPauseButton;
        TextView currentTime;
        TextView totalTime;



        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackImage = itemView.findViewById(R.id.track_image);
            trackTitle = itemView.findViewById(R.id.track_title);
            trackDescription = itemView.findViewById(R.id.track_description);
            seekBar = itemView.findViewById(R.id.seekBar);
            playPauseButton = itemView.findViewById(R.id.playPauseButton);
            currentTime = itemView.findViewById(R.id.current_time);
            totalTime = itemView.findViewById(R.id.total_time);
        }
    }
}

