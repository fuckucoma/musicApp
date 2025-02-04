package com.example.music.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.music.request.ReviewRequest;
import com.example.music.view_model.ReviewViewModel;
import com.example.test.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

public class AddReviewDialog extends DialogFragment {

    private static final String ARG_TRACK_ID = "arg_track_id";
    private int trackId;

    private ReviewViewModel reviewViewModel;

    public static void show(@NonNull androidx.fragment.app.FragmentManager fm, int trackId) {
        AddReviewDialog dialog = new AddReviewDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_TRACK_ID, trackId);
        dialog.setArguments(args);
        dialog.show(fm, "AddReviewDialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        if (getArguments() != null) {
            trackId = getArguments().getInt(ARG_TRACK_ID, -1);
        }

        reviewViewModel = new ViewModelProvider(requireActivity()).get(ReviewViewModel.class);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Добавить отзыв");

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        final android.view.View dialogView = inflater.inflate(R.layout.dialog_add_review, null);
        builder.setView(dialogView);

        final TextInputLayout etReviewContent = dialogView.findViewById(R.id.etReviewContent);
        final RatingBar rbRating = dialogView.findViewById(R.id.rbRating);

        builder.setPositiveButton("Отправить", (dialog, which) -> {
            String content = etReviewContent.getEditText().getText().toString().trim();
            float rating = rbRating.getRating(); // от 0 до 5

            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "Введите текст отзыва", Toast.LENGTH_SHORT).show();
                return;
            }

            ReviewRequest reviewRequest = new ReviewRequest(trackId, content, (int) rating);
            reviewViewModel.createReview(reviewRequest);

        });

        builder.setNegativeButton("Отмена", (dialog, which) -> {
            dialog.dismiss();
        });

        return builder.create();
    }
}
