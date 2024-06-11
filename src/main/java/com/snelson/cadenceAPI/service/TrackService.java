package com.snelson.cadenceAPI.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TrackService {

    @Autowired
    private SpotifyApiService spotifyApiService;

    @Async
    public CompletableFuture<Paging<Track>> getTracksAsync(String[] queries) {
        try {
            List<CompletableFuture<Paging<Track>>> futures = Stream.of(queries)
                    .map(query -> spotifyApiService.spotifyApi.searchTracks(query)
                            .includeExternal("audio")
                            .build()
                            .executeAsync())
                    .toList();

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            return allFutures.thenApply(v -> futures.stream()
                    .map(CompletableFuture::join)
                    .reduce((a, b) -> {
                        List<Track> items = new ArrayList<>();
                        items.addAll(List.of(a.getItems()));
                        items.addAll(List.of(b.getItems()));
                        return a;
                    }).orElse(null));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Async
    public CompletableFuture<Paging<Track>> getTrackAsync (String query) {
        try {
            System.out.println("Searching for track async: " + query);
            return CompletableFuture.completedFuture(spotifyApiService.spotifyApi.searchTracks(query).build().execute());
        } catch (Exception e) {
            System.out.println("Error getting track async: " + e.getMessage());
        }
        return null;
    }
}