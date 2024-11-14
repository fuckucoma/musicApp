package com.example.music.api;

import com.example.music.FavoriteRequest;
import com.example.music.models.Favorite;
import com.example.music.models.LoginResponse;
import com.example.music.models.RegisterResponse;
import com.example.music.models.Track;
import com.example.music.models.User;
import com.example.music.models.UserProfileResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
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

    @POST("/favorites/add")
    Call<FavoriteResponse> addFavorite(@Body FavoriteRequest trackId);

    @GET("/favorites/get")
    Call<FavoriteResponse> getFavorites();

    @GET("/tracks/search")
    Call<List<Track>> searchTracks(@Query("query") String query);
}
