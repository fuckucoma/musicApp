package com.example.music.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull; // AndroidX
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music.models.Review;
import com.example.music.repository.AdminRepository;
import com.example.music.repository.ReviewRepository;
import com.example.test.R;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private final List<Review> reviewList;
    private final int currentUserId;

    public ReviewAdapter(Context context, List<Review> reviewList, int currentUserId) {
        this.context = context;
        this.reviewList = reviewList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reviews, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Review review = reviewList.get(position);

        if (review.getUser() != null) {
            holder.userName.setText(review.getUser().getUsername());

            if (review.getUser().getProfileImageUrl() != null) {
                Glide.with(context)
                        .load(review.getUser().getProfileImageUrl())
                        .into(holder.userAvatar);
            }

            // Проверка, является ли текущий пользователь владельцем отзыва
            if (review.getUser().getId() == currentUserId) {
                holder.deleteButton.setVisibility(View.VISIBLE);  // Показываем кнопку удаления
            } else {
                holder.deleteButton.setVisibility(View.GONE); // Скрываем кнопку удаления для чужих отзывов
            }
        } else {
            holder.userName.setText("Unknown User");
        }

        holder.rating.setText("Rating: " + review.getRating());
        holder.content.setText(review.getContent());

        holder.deleteButton.setOnClickListener(v -> {
            // Вызываем пользовательский метод удаления (не админский)
            ReviewRepository.getInstance().deleteUserReview(review.getId(), new ReviewRepository.MyCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    // Удаляем из списка и обновляем адаптер
                    reviewList.remove(position);
                    notifyItemRemoved(position);
                }

                @Override
                public void onError(Throwable t) {
                    Toast.makeText(context, "Ошибка: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        ImageView userAvatar;
        ImageView deleteButton;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.review_user_name);
            userAvatar = itemView.findViewById(R.id.review_user_avatar);
            rating = itemView.findViewById(R.id.review_rating);
            content = itemView.findViewById(R.id.review_content);
            deleteButton = itemView.findViewById(R.id.button_delete_review);
        }
    }
}
