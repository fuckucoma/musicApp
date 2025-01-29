package com.example.music.view_model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.music.models.Review;
import com.example.music.repository.ReviewRepository;
import com.example.music.repository.TrackRepository;
import com.example.music.request.ReviewRequest;

import java.util.List;

public class ReviewViewModel extends AndroidViewModel {

    private ReviewRepository reviewRepository;
    private MutableLiveData<List<Review>> reviewsLiveData = new MutableLiveData<>();

    public ReviewViewModel(Application application) {
        super(application);
        reviewRepository = ReviewRepository.getInstance();
        reviewsLiveData = (MutableLiveData<List<Review>>) reviewRepository.getReviewsLiveData();
    }

    public void fetchReviewsForTrack(int trackId) {
        reviewRepository.fetchReviewsForTrack(trackId);
    }

    public void createReview(ReviewRequest request) {
        reviewRepository.createReview(request);
    }

    public LiveData<List<Review>> getReviewsLiveData() {
        return reviewsLiveData;
    }
}
