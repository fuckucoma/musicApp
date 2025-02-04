package com.example.music.view_model;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.music.models.Track;
import com.example.music.repository.TrackRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchViewModel extends AndroidViewModel {
    private TrackRepository trackRepository;
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<List<Track>> searchHistory = new MutableLiveData<>(new ArrayList<>());

    public SearchViewModel(Application application) {
        super(application);
        trackRepository = TrackRepository.getInstance();
        searchHistory.setValue(getSearchHistoryMama());
        Log.e("History","история :" + searchHistory.getValue().toString());
    }

    public LiveData<List<Track>> getSearchTracks() {
        return trackRepository.getSearchTracks();
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<List<Track>> getSearchHistory() {
        return searchHistory;
    }

    public void searchTracks(String query) {
        trackRepository.searchTracks(query);
        // Аналогично, можно расширить репозиторий,
        // чтобы при ошибке postValue в errorMessage
    }

    public void addToSearchHistory(Track track) {
        List<Track> currentHistory = searchHistory.getValue();
        if (currentHistory == null) currentHistory = new ArrayList<>();
        if (!currentHistory.contains(track)) {
            if (currentHistory.size() >= 8) {
                currentHistory.remove(0);
            }
            currentHistory.add(track);
        }

        searchHistory.setValue(currentHistory);
        Log.e("History","добавленный трек в историю :" + currentHistory.toString());

        Gson gson = new Gson();
        String jsonHistory = gson.toJson(currentHistory);

        // Сохраняем строку JSON в SharedPreferences
        SharedPreferences prefs = getApplication().getSharedPreferences("SearchHistory", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("history", jsonHistory);
        editor.apply();

    }

    public List<Track> getSearchHistoryMama() {
        SharedPreferences prefs = getApplication().getSharedPreferences("SearchHistory", Context.MODE_PRIVATE);
        String jsonHistory = prefs.getString("history", "[]");  // по умолчанию возвращаем пустой список

        Gson gson = new Gson();
        Type type = new TypeToken<List<Track>>() {}.getType();  // Тип для списка Track
        return gson.fromJson(jsonHistory, type);  // Десериализуем строку JSON обратно в список
    }
}
