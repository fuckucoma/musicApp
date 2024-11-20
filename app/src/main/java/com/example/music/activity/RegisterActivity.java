package com.example.music.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.music.response.RegisterResponse;
import com.example.music.models.User;
import com.example.music.api.ApiClient;
import com.example.music.api.ApiService;
import com.example.test.R;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText etUsername, etPassword, etConfirmPassword;
    private Button btnRegister;
    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.register_activity); // Убедитесь, что у вас есть соответствующий макет

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        // Используем getClient без токена, так как при регистрации токен ещё нет
        apiService = ApiClient.getClient().create(ApiService.class);

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validateInput(username, password, confirmPassword)) {
                User user = new User(username, password);
                registerUser(user);
            }
        });

        tvLoginLink.setOnClickListener(v -> {
            // Перейти на экран входа
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateInput(String username, String password, String confirmPassword) {
        if (username.isEmpty()) {
            etUsername.setError("Введите имя пользователя");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Введите пароль");
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Пароль должен содержать не менее 6 символов");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Пароли не совпадают");
            return false;
        }

        return true;
    }

    private void registerUser(User user) {
        Call<RegisterResponse> call = apiService.register(user);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    RegisterResponse registerResponse = response.body();
                    if (registerResponse != null && registerResponse.getToken() != null) {
                        Toast.makeText(RegisterActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                        saveAuthToken(registerResponse.getToken());

                        // Переход к основной активности
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Ошибка получения токена", Toast.LENGTH_SHORT).show();
                        // Перейти на экран входа
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(RegisterActivity.this, "Ошибка регистрации: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAuthToken(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("authToken", token);
        editor.apply();
    }
}
