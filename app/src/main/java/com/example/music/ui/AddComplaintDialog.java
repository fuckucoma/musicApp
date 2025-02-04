package com.example.music.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.music.repository.ComplaintRepository;
import com.example.music.request.ComplaintRequest;
import com.example.test.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

public class AddComplaintDialog extends DialogFragment {

    private static final String ARG_TRACK_ID = "arg_track_id";
    private int trackId;

    public static void show(@NonNull androidx.fragment.app.FragmentManager fm, int trackId) {
        AddComplaintDialog dialog = new AddComplaintDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_TRACK_ID, trackId);
        dialog.setArguments(args);
        dialog.show(fm, "AddComplaintDialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            trackId = getArguments().getInt(ARG_TRACK_ID, -1);
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Отправить жалобу");

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        final android.view.View dialogView = inflater.inflate(R.layout.dialog_complaint, null);
        builder.setView(dialogView);

        final TextInputLayout etComplaintText = dialogView.findViewById(R.id.etComplaintText);

        builder.setPositiveButton("Отправить", (dialog, which) -> {
            String message = etComplaintText.getEditText().getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(requireContext(), "Введите текст жалобы", Toast.LENGTH_SHORT).show();
                return;
            }

            ComplaintRequest request = new ComplaintRequest(trackId, message);
            ComplaintRepository.getInstance().createComplaint(request, new ComplaintRepository.MyCallback<Void>() {
                public void onSuccess(Void data) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Жалоба отправлена!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Throwable t) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Ошибка: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}
