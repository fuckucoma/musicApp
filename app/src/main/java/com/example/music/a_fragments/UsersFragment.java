package com.example.music.a_fragments;

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

import com.example.music.adapters.UsersAdapter;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.music.models.User;
import com.example.music.response.UserProfileResponse;
import com.example.music.response.UsersResponse;
import com.example.test.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText search_user;
    private Button search_button;
    private UsersAdapter adapter;
    private List<User> users = new ArrayList<>();
    private ApiService apiService;
    private UserProfileResponse userProfileResponse;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.users);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        search_user = view.findViewById(R.id.search_user);
        search_button = view.findViewById(R.id.search_button);

        apiService = ApiClient.getClient().create(ApiService.class);

        adapter = new UsersAdapter(users, this::deleteUser);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void fetchUsers() {
        Call<UsersResponse> call = apiService.getAllUsers();
        call.enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(Call<UsersResponse> call, Response<UsersResponse> response) {
                UsersResponse usersResponse = response.body();
//                users.clear();
//                users.add(usersResponse.)
//                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<UsersResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка загрузки пользователей", Toast.LENGTH_SHORT).show();
            }


        });
    }

    private void deleteUser(int userId) {
       Call<User> call = apiService.deleteUser();
       call.enqueue(new Callback<User>() {
           @Override
           public void onResponse(Call<User> call, Response<User> response) {
               adapter.notifyDataSetChanged();
           }

           @Override
           public void onFailure(Call<User> call, Throwable t) {

           }
       });
    }
}