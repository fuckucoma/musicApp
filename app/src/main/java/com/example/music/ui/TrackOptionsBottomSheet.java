package com.example.music.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.test.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TrackOptionsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_TRACK_ID = "arg_track_id";
    private int trackId;

    public static TrackOptionsBottomSheet newInstance(int trackId) {
        TrackOptionsBottomSheet fragment = new TrackOptionsBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_TRACK_ID, trackId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trackId = getArguments().getInt(ARG_TRACK_ID, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_track_options, container, false);
        View btnAddReview = view.findViewById(R.id.btn_add_review);
        View btnComplain  = view.findViewById(R.id.btn_complain);

        btnAddReview.setOnClickListener(v -> {
            dismiss();
            showAddReviewDialog(trackId);
        });

        btnComplain.setOnClickListener(v -> {
            dismiss();
            showComplaintDialog(trackId);
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);

                // Фиксируем высоту по содержимому
                behavior.setFitToContents(true);

                // Запрещаем сворачивание при клике вне области
                behavior.setHideable(false);

                behavior.setDraggable(true);

                if (isTablet()) {
                    bottomSheet.setLayoutParams(
                            new FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    Gravity.CENTER
                            )
                    );
                }
            }
        }
    }

    private boolean isTablet() {
        return getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    private void showAddReviewDialog(int trackId) {
        AddReviewDialog.show(requireActivity().getSupportFragmentManager(), trackId);
    }

    private void showComplaintDialog(int trackId) {
        AddComplaintDialog.show(requireActivity().getSupportFragmentManager(), trackId);
    }
}