package com.example.music.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.music.response.LoginResponse;
import com.example.music.models.User;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.test.R;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ApiService apiService;
    private TextView tvRegisterLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInput(username, password)) {
                User user = new User(username, password);
                loginUser(user);
            }
        });

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty()) {
            etUsername.setError("Введите имя пользователя");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Введите пароль");
            return false;
        }

        return true;
    }

    private void loginUser(User user) {
        Call<LoginResponse> call = apiService.login(user);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse != null && loginResponse.getToken() != null) {
                        Toast.makeText(LoginActivity.this, "Вход выполнен успешно", Toast.LENGTH_SHORT).show();
                        saveAuthToken(loginResponse.getToken(), loginResponse.getAdmin());

                        Intent intent;
                        if (!loginResponse.getAdmin()) {
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, AdminActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Ошибка получения токена", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(LoginActivity.this, "Ошибка входа: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Ошибка входа", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("Login", "Ошибка сети: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAuthToken(String token , boolean admin) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("authToken", token);
        editor.putBoolean("isAdmin", admin);
        editor.apply();
    }

}
