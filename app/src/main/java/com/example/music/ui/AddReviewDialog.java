package com.example.music.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.music.repository.RevComRepository;
import com.example.music.request.ReviewRequest;
import com.example.test.R;

public class AddReviewDialog extends DialogFragment {

    private static final String ARG_TRACK_ID = "arg_track_id";
    private int trackId;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Добавить отзыв");

        // Подключаем layout для диалога
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        final android.view.View dialogView = inflater.inflate(R.layout.dialog_add_review, null);
        builder.setView(dialogView);

        final EditText etReviewContent = dialogView.findViewById(R.id.etReviewContent);
        final RatingBar rbRating = dialogView.findViewById(R.id.rbRating);

        builder.setPositiveButton("Отправить", (dialog, which) -> {
            String content = etReviewContent.getText().toString().trim();
            float rating = rbRating.getRating(); // от 0 до 5

            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "Введите текст отзыва", Toast.LENGTH_SHORT).show();
                return;
            }

            ReviewRequest request = new ReviewRequest(trackId, content, (int) rating);
            RevComRepository.getInstance().createReview(request, new RevComRepository.MyCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Отзыв добавлен!", Toast.LENGTH_SHORT).show();
                    dismiss();
                }

                @Override
                public void onError(Throwable t) {

                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Ошибка: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

        });

        builder.setNegativeButton("Отмена", (dialog, which) -> {
            dialog.dismiss();
        });

        return builder.create();
    }
}
