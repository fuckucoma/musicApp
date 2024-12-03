package com.example.music.api;

import com.example.music.FavoriteRequest;
import com.example.music.response.FavoriteResponse;
import com.example.music.response.LoginResponse;
import com.example.music.response.RegisterResponse;
import com.example.music.models.Track;
import com.example.music.models.User;
import com.example.music.response.TrackResponse;
import com.example.music.response.UserProfileResponse;
import com.example.music.response.UsersResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/users/register")
    Call<RegisterResponse> register(@Body User user);

    @POST("/users/login")
    Call<LoginResponse> login(@Body User user);

    @POST("/users/logout")
    Call<Void> logout();

    @GET("/users/profile")
    Call<UserProfileResponse> getUserProfile();

    @Multipart
    @POST("/users/uploadProfileImage")
    Call<RegisterResponse> uploadProfileImage(
            @Part MultipartBody.Part profileImage
    );

    @GET("users/all")
    Call<UsersResponse> getAllUsers();

    @GET("tracks/")
    Call<List<Track>> getAllTracks();

    @DELETE("tracks/delete/{id}") // Замените на ваш конечный путь
    Call<Void> deleteTrack(@Path("id") int id);

    @DELETE("users/delete/{id}")
    Call<Void>deleteUser(@Path("id") int id);

    @GET("/favorites/get")
    Call<FavoriteResponse> getFavorites();

    @POST("/favorites/remove")
    Call<FavoriteResponse> removeFavorite(@Body FavoriteRequest favoriteRequest);

    @POST("/favorites/add")
    Call<FavoriteResponse> addFavorite(@Body FavoriteRequest favoriteRequest);

    @GET("/tracks/search")
    Call<List<Track>> searchTracks(@Query("query") String query);
}
