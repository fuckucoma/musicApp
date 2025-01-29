package com.example.music.AdminPanel;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.music.adapters.AdminReviewAdapter;
import com.example.music.models.Review;
import com.example.music.repository.AdminRepository;
import com.example.test.R;

import java.util.List;

public class ReviewFragment extends Fragment {

    private RecyclerView reviewsRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);
        reviewsRecyclerView = view.findViewById(R.id.reviews_recycler_view);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchReviews();
        return view;
    }

    private void fetchReviews() {
        AdminRepository.getInstance().fetchReviews(new AdminRepository.MyCallback<List<Review>>() {
            @Override
            public void onSuccess(List<Review> data) {
                AdminReviewAdapter adapter = new AdminReviewAdapter(getContext(), data);
                reviewsRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onError(Throwable t) {
                Log.e("ReviewFragment", "Ошибка загрузки отзывов: " + t.getMessage());
            }
        });
    }
}
