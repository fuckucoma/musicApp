package com.example.music.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.test.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;

public class ApiClient {

    private static Retrofit retrofit = null;
    private static Context appContext;

    // Метод для инициализации с контекстом приложения
    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

            // Добавление interceptor для авторизации
            clientBuilder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder();

                    // Получение токена из SharedPreferences
                    SharedPreferences sharedPreferences = appContext.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("authToken", null);

                    if (token != null && !token.isEmpty()) {
                        requestBuilder.header("Authorization", "Bearer " + token);
                    }

                    Request request = requestBuilder.build();
                    Log.d("ApiClient", "Запрос: " + request.method() + " " + request.url());
                    if (request.body() != null) {
                        Log.d("ApiClient", "Тело запроса: " + request.body().toString());
                    }

                    return chain.proceed(request);
                }
            });

            // Добавление interceptor для логирования запросов и ответов
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(logging);

            OkHttpClient client = clientBuilder.build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
