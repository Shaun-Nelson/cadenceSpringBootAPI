package com.snelson.cadenceAPI.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@Service
public class TrackService {

    @Autowired
    private SpotifyApiService spotifyApiService;

    @Async("taskExecutor")
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
                        return new Paging.Builder<Track>()
                                .setItems(items.toArray(new Track[0]))
                                .setTotal(a.getTotal() + b.getTotal())
                                .build();
                    }).orElse(null));
        } catch (Exception e) {
            System.out.println("Error getting tracks async: " + e.getMessage());
            return CompletableFuture.completedFuture(new Paging.Builder<Track>().build());
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<Paging<Track>> getTrackAsync (String query) {
        try {
            System.out.println("Searching for track async: " + query);
            SearchTracksRequest searchTracksRequest = spotifyApiService.spotifyApi.searchTracks(query)
                    .includeExternal("audio")
                    .build();
            return searchTracksRequest.executeAsync();
        } catch (Exception e) {
            System.out.println("Error getting track async: " + e.getMessage());
        }
        return null;
    }

    @Async
    public CompletableFuture<Track[]> getTracksFromJsonAsyncNew(String jsonResponse) {
        try {
            Gson gson = new Gson();
            JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);
            String[] queries = new String[jsonArray.size()];

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                queries[i] = jsonObject.get("title") + " " + jsonObject.get("artist");
            }
            CompletableFuture<Paging<Track>> pagingFuture = getTracksAsync(queries);
            Paging<Track> paging = pagingFuture.get();
            return CompletableFuture.completedFuture(paging.getItems());
        } catch (IllegalStateException e) {
            System.out.println("Error getting tracks from json async: " + e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}