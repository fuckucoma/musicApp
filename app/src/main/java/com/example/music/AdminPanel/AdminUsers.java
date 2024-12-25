package com.example.music.AdminPanel;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.music.adapters.AUsersAdapter;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.Users;
import com.example.music.response.UsersResponse;
import com.example.test.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsers extends Fragment {

    private RecyclerView recyclerView;
    private EditText search_user;
    private Button search_button;
    private AUsersAdapter adapter;
    private List<Users> users = new ArrayList<>();
    private ApiService apiService;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_users, container, false);

        recyclerView = view.findViewById(R.id.users);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        search_user = view.findViewById(R.id.search_user);
        search_button = view.findViewById(R.id.search_button);

        apiService = ApiClient.getClient().create(ApiService.class);

        adapter = new AUsersAdapter(users, this::deleteUser);
        recyclerView.setAdapter(adapter);
        fetchUsers();
        return view;
    }

    private void fetchUsers() {
        Call<UsersResponse> call = apiService.getAllUsers();
        call.enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(Call<UsersResponse> call, Response<UsersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UsersResponse usersResponse = response.body();
                    adapter.updateData(usersResponse.getUsers());
                } else {
                    Toast.makeText(getContext(), "Ошибка получения данных", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UsersResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка загрузки пользователей", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser(int userId) {
        Call<Void> call = apiService.deleteUser(userId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Пользователь удален", Toast.LENGTH_SHORT).show();
                    fetchUsers(); // Обновляем список пользователей
                } else {
                    Toast.makeText(getContext(), "Ошибка удаления пользователя", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка удаления пользователя", Toast.LENGTH_SHORT).show();
            }
        });
    }
}