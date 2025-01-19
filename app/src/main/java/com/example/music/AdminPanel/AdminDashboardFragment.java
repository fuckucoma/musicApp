package com.example.music.AdminPanel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable; // AndroidX
import androidx.annotation.NonNull; // AndroidX
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.music.activity.LoginActivity;
import com.example.music.models.DashboardData;
import com.example.music.repository.AdminRepository;
import com.example.test.R;

public class AdminDashboardFragment extends Fragment {

    private TextView usersCount, tracksCount, complaintsCount, reviewsCount;
    private ImageButton btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        usersCount = view.findViewById(R.id.users_count);
        tracksCount = view.findViewById(R.id.tracks_count);
        complaintsCount = view.findViewById(R.id.complaints_count);
        reviewsCount = view.findViewById(R.id.reviews_count);
        btnLogout = view.findViewById(R.id.btn_logout);

        btnLogout.setOnClickListener(v -> logoutUser());

        fetchDashboardData();
        return view;
    }

    private void fetchDashboardData() {
        AdminRepository.getInstance().fetchDashboardData(new AdminRepository.MyCallback<DashboardData>() {
            @Override
            public void onSuccess(DashboardData data) {
                usersCount.setText(String.valueOf(data.getUsers()));
                Log.e("dashboard","Пользователи: " + usersCount);
                tracksCount.setText(String.valueOf(data.getTracks()));
                complaintsCount.setText(String.valueOf(data.getComplaints()));
                reviewsCount.setText(String.valueOf(data.getReviews()));
            }

            @Override
            public void onError(Throwable t) {
                Log.e("AdminDashboardFragment", "Ошибка загрузки данных панели: " + t.getMessage());
            }
        });
    }

    private void logoutUser() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("authToken");  // Удаление токена
        editor.remove("isAdmin");    // Удаление флага администратора
        editor.apply();

        // Переход к экрану входа
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();  // Закрытие текущей активности
    }
}
