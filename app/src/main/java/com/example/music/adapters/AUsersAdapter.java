package com.example.music.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music.models.Users;
import com.example.test.R;

import java.util.List;

public class AUsersAdapter extends RecyclerView.Adapter<AUsersAdapter.UserViewHolder> {

    private List<Users> users;
    private OnUserDeleteListener deleteListener;

    public interface OnUserDeleteListener {
        void onDelete(int userId);
    }

    public AUsersAdapter(List<Users> users, OnUserDeleteListener deleteListener) {
        this.users = users;
        this.deleteListener = deleteListener;
    }

    public void updateData(List<Users> newUsers) {
        users.clear();
        users.addAll(newUsers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_users_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Users user = users.get(position);
        holder.username.setText(user.getUsername());
        holder.createdAt.setText(user.getCreatedAt());

        if (user.getProfileImageUrl() != null) {
            Glide.with(holder.imageView.getContext())
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.placeholder_image) // Замените на ваш ресурс по умолчанию
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_image); // Замените на ваш ресурс по умолчанию
        }

        holder.deleteButton.setOnClickListener(v -> deleteListener.onDelete(user.getId()));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        ImageView imageView;
        Button deleteButton;
        TextView createdAt;

        public UserViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.track_image);
            username = itemView.findViewById(R.id.username);
            createdAt = itemView.findViewById(R.id.createdAt);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}