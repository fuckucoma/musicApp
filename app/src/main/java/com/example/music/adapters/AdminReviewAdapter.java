package com.example.music.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull; // AndroidX

import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.music.models.Review;
import com.example.music.repository.AdminRepository;
import com.example.music.repository.ProfileRepository;
import com.example.music.view_model.ProfileViewModel;
import com.example.test.R;

import java.util.List;

public class AdminReviewAdapter extends RecyclerView.Adapter<AdminReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private final List<Review> reviewList;
    private final ProfileViewModel profileViewModel;

    public AdminReviewAdapter(Context context, List<Review> reviewList, ProfileViewModel profileViewModel) {
        this.context = context;
        this.reviewList = reviewList;
        this.profileViewModel = profileViewModel;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review_admin, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Review review = reviewList.get(position);

        if (review.getUser() != null) {
            holder.userName.setText(review.getUser().getUsername());

            profileViewModel.getUserProfile().observe((LifecycleOwner) context, userProfile -> {
                if (userProfile != null && userProfile.getProfileImageUrl() != null) {
                    // Загрузка изображения аватарки пользователя через Glide
                    Glide.with(context)
                            .load(userProfile.getProfileImageUrl())
                            .placeholder(R.drawable.placeholder_image)
                            .into(holder.review_user_avatar);
                } else {
                    holder.review_user_avatar.setImageResource(R.drawable.placeholder_image);
                }
            });
            Log.d("ADMIN", "url: " + review.getUser().getProfileImageUrl());

        } else {
            holder.userName.setText("Unknown User");
        }

//        if (review.getTrack() != null) {
//            holder.trackTitle.setText(review.getTrack().getTitle());
//        } else {
//            holder.trackTitle.setText("Unknown Track");
//        }

        holder.rating.setText("Rating: " + review.getRating());
        holder.content.setText(review.getContent());

        // Удаление отзыва
        holder.deleteButton.setOnClickListener(v -> {
            AdminRepository.getInstance().deleteReview(review.getId(), new AdminRepository.MyCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    reviewList.remove(position);
                    notifyItemRemoved(position);
                }

                @Override
                public void onError(Throwable t) {

                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView userName, rating, content;
        ImageView deleteButton, review_user_avatar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.review_user_name);
//            trackTitle = itemView.findViewById(R.id.review_track_title);
            rating = itemView.findViewById(R.id.review_rating);
            content = itemView.findViewById(R.id.review_content);
            deleteButton = itemView.findViewById(R.id.button_delete_review);
            review_user_avatar = itemView.findViewById(R.id.review_user_avatar);
        }
    }
}
