package com.example.music;

import java.util.ArrayList;
import java.util.List;

public class FavoriteManager {
    private final List<Integer> favoriteTrackIds = new ArrayList<>();

    public void setFavoritesFromServer(List<Integer> trackIds) {
        favoriteTrackIds.clear();
        favoriteTrackIds.addAll(trackIds);
    }

    public boolean isTrackFavorite(int trackId) {
        return favoriteTrackIds.contains(trackId);
    }

    public void addTrackToFavorites(int trackId) {
        if (!favoriteTrackIds.contains(trackId)) {
            favoriteTrackIds.add(trackId);
        }
    }

    public void removeTrackFromFavorites(int trackId) {
        favoriteTrackIds.remove((Integer) trackId);
    }

    public List<Integer> getFavoriteTrackIds() {
        return new ArrayList<>(favoriteTrackIds);
    }
}
