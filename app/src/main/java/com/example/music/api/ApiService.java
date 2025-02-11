package com.example.music.api;

import com.example.music.models.Complaint;
import com.example.music.request.ComplaintRequest;
import com.example.music.models.DashboardData;
import com.example.music.request.FavoriteRequest;
import com.example.music.models.Review;
import com.example.music.request.ReviewRequest;
import com.example.music.request.TrackUpdateRequest;
import com.example.music.request.passRequest;
import com.example.music.request.usernameRequest;
import com.example.music.response.FavoriteResponse;
import com.example.music.response.LoginResponse;
import com.example.music.response.RegisterResponse;
import com.example.music.models.Track;
import com.example.music.models.User;
import com.example.music.response.UserProfileResponse;
import com.example.music.response.UsersResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @PUT("/users/edit-pass")
    Call<ResponseBody> editPassword(@Body passRequest request);

    @PUT("/users/edit-username")
    Call<ResponseBody> editUsername(@Body usernameRequest request);

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

    // ========== Треки

    @GET("tracks/")
    Call<List<Track>> getAllTracks();

//    @DELETE("tracks/delete/{id}")
//    Call<Void> deleteTrack(@Path("id") int id);

    @DELETE("users/delete/{id}")
    Call<Void>deleteUser(@Path("id") int id);

    @GET("/favorites/get")
    Call<FavoriteResponse> getLibraryTracks();

    @POST("/favorites/remove")
    Call<FavoriteResponse> removeFavorite(@Body FavoriteRequest favoriteRequest);

    @POST("/favorites/add")
    Call<FavoriteResponse> addFavorite(@Body FavoriteRequest favoriteRequest);

    @GET("/tracks/search")
    Call<List<Track>> searchTracks(@Query("query") String query);

    // ======= АДМИН (dashboard, complaints, reviews, tracks) =======
    @GET("/api/admin/dashboard")
    Call<DashboardData> getDashboardData();

    @GET("/api/admin/complaints")
    Call<List<Complaint>> getAllComplaints();

    @PUT("/api/admin/complaints/{id}")
    Call<Void> updateComplaint(@Path("id") int id, @Body ComplaintRequest request);

    @GET("/api/admin/reviews")
    Call<List<Review>> getAllReviews();

    @DELETE("/api/admin/reviews/{id}")
    Call<Void> deleteReview(@Path("id") int id);

    @PUT("/api/admin/tracks/{id}")
    Call<Void> updateTrack(@Path("id") int id, @Body TrackUpdateRequest request);

    @DELETE("/api/admin/tracks/{id}")
    Call<Void> deleteTrack(@Path("id") int id);

    @GET("/users/users/{id}")
    Call<UserProfileResponse> getUserById(@Path("id") int id);

    // ============== отзывы и жалобы
    @POST("/reviews/create")
    Call<Void> createReview(@Body ReviewRequest reviewRequest);

    @GET("/reviews/track/{trackId}")
    Call<List<Review>> getReviewsForTrack(@Path("trackId") int trackId);

    @POST("/complaints/create")
    Call<Void> createComplaint(@Body ComplaintRequest complaintRequest);

    @DELETE("/reviews/delete_user_review/{id}")
    Call<Void> deleteUserReview(@Path("id") int reviewId);
}
