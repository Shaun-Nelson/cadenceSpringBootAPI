package com.snelson.cadenceAPI.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.snelson.cadenceAPI.dto.SearchTracksAsyncResponse;
import com.snelson.cadenceAPI.dto.SpotifyPlaylistRequestSong;
import com.snelson.cadenceAPI.model.Song;
import com.snelson.cadenceAPI.utils.CustomGsonExclusionStrategy;
import com.snelson.cadenceAPI.utils.SecureRandomTypeAdapter;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@Log
public class SpotifyApiService {

    @Autowired
    private CloseableHttpClient httpClient;

    @Value("${CLIENT_ID}")
    private String CLIENT_ID;

    @Value("${CLIENT_SECRET}")
    private String CLIENT_SECRET;

    @Value("${REDIRECT_URI}")
    private String REDIRECT_URI;

    @Value("${CLIENT_URL}")
    private String CLIENT_URL;

    @Value("${ENV}")
    private String ENV;

    public String STATE;
    public String SCOPE;
    public Gson gson;

    @Autowired
    public SpotifyApi spotifyApi;

    @PostConstruct
    public void init() {
        this.STATE = generateRandomString(16);
        this.SCOPE = "playlist-modify-public playlist-modify-private playlist-read-private playlist-read-collaborative user-read-private user-read-email";

        this.gson = new GsonBuilder()
                .setExclusionStrategies(new CustomGsonExclusionStrategy())
                .registerTypeAdapter(SecureRandom.class, new SecureRandomTypeAdapter())
                .create();
        log.info("SpotifyApiService initialized");
    }

    public void refreshSync() {
        try {
            AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
            spotifyApi.setAccessToken(authorizationCodeRefreshRequest.execute().getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeRefreshRequest.execute().getRefreshToken());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error refreshing Spotify access token: " + e.getMessage());
        }
    }

    private void clientCredentials_Sync() {
        try {
            final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
            spotifyApi.setAccessToken(clientCredentialsRequest.execute().getAccessToken());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error getting Spotify access token: " + e.getMessage());
        }
    }

    public User getCurrentUser() throws IOException, SpotifyWebApiException, ParseException {
        GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = spotifyApi.getCurrentUsersProfile().build();
        return getCurrentUsersProfileRequest.execute();
    }

    public Playlist createPlaylist(String userId, String playlistName, String description) throws IOException, SpotifyWebApiException, ParseException {
        CreatePlaylistRequest createPlaylistRequest = spotifyApi.createPlaylist(userId, playlistName).description(description).build();
        return createPlaylistRequest.execute();
    }

    public void addSongsToPlaylist(String playlistId, String[] trackIds) throws IOException, ParseException, SpotifyWebApiException {
        spotifyApi.addItemsToPlaylist(playlistId, trackIds).build().execute();
    }

    public void checkSpotifyCredentials() {
        clientCredentials_Sync();
    }

    public String[] getTrackIds(SpotifyPlaylistRequestSong[] songs) {
        List<String> trackIds = new ArrayList<>();
        for (SpotifyPlaylistRequestSong song : songs) {
            trackIds.add(song.getSpotifyId());
        }
        return trackIds.toArray(new String[0]);
    }

    public Track searchTrack(String query) {
        try {
            checkSpotifyCredentials();
            return spotifyApi.searchItem(query, "track").build().execute().getTracks().getItems()[0];
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error searching track: " + e.getMessage());
            return null;
        }
    }

    public List<Track> getSpotifySongs(String[] trackIds) {
        checkSpotifyCredentials();

        List<Track> tracks = new ArrayList<>();

        for (String trackId : trackIds) {
            try {
                tracks.add(spotifyApi.getTrack(trackId).build().execute());
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.out.println("Error getting Spotify tracks: " + e.getMessage());
            }
        }
        return tracks;
    }

    public String generateRandomString(int length) {
        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        while (length-- != 0) {
            int character = (int) (Math.random() * alphaNumericString.length());
            builder.append(alphaNumericString.charAt(character));
        }
        return builder.toString();
    }

    public void setCookies(int accessTokenExpiresIn, HttpServletResponse response) {
        try {
            int EXPIRES_IN = 60 * 60 * 24 * 30;
            log.info("Setting cookies. Refresh token: " + spotifyApi.getRefreshToken() + " Access token: " + spotifyApi.getAccessToken());
            Cookie accessToken = new Cookie("access_token", spotifyApi.getAccessToken());
            accessToken.setMaxAge(accessTokenExpiresIn);
            accessToken.setSecure(ENV.equals("production"));
            accessToken.setPath("/");
            response.addCookie(accessToken);

            Cookie refreshToken = new Cookie("refresh_token", spotifyApi.getRefreshToken());
            refreshToken.setMaxAge(EXPIRES_IN);
            refreshToken.setSecure(ENV.equals("production"));
            refreshToken.setPath("/");
            response.addCookie(refreshToken);

            Cookie expiresIn = new Cookie("expires_in", String.valueOf(new Date().toInstant().plusSeconds(EXPIRES_IN).getEpochSecond()));
            expiresIn.setMaxAge(EXPIRES_IN);
            expiresIn.setSecure(ENV.equals("production"));
            expiresIn.setPath("/");
            response.addCookie(expiresIn);

            log.info("Cookies set");
        } catch (Exception e) {
            System.out.println("Error setting cookies: " + e.getMessage());
        }
    }

    public Track[] getTracksSync(String[] queries) {
        try {
            Track[] tracks = new Track[queries.length];
            for (int i = 0; i < queries.length; i++) {
                System.out.println("Searching for track: " + queries[i]);
                Paging<Track> paging = spotifyApi.searchTracks(queries[i]).build().execute();
                tracks[i] = paging.getItems()[0];
            }
            return tracks;
        } catch (Exception e) {
            System.out.println("Error getting track sync: " + e.getMessage());
        }
        return new Track[0];
    }

    @Async("taskExecutor")
    public CompletableFuture<Track[]> getTracksAsync(String[] queries) {
        try {
            List<CompletableFuture<Track>> futures = new ArrayList<>();
            for (String query : queries) {
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        System.out.println("Searching for track async: " + query);
                        Paging<Track> paging = spotifyApi.searchTracks(query).build().execute();
                        Track[] tracks = paging.getItems();
                        return (tracks.length > 0) ? tracks[0] : null;  // Return the first track if available
                    } catch (IOException | SpotifyWebApiException | ParseException e) {
                        System.out.println("Error getting track async: " + e.getMessage());
                        return null;
                    }
                }));
            }

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            return allFutures.thenApply(v -> futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull) // Ensure null tracks are not included
                    .toArray(Track[]::new));
        } catch (Exception e) {
            System.out.println("Error getting tracks async: " + e.getMessage());
            return CompletableFuture.completedFuture(new Track[0]);
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<List<Song>> searchTracksAsync(String[] queries) {
        checkSpotifyCredentials();

        final String ENDPOINT = "https://api.spotify.com/v1/search";
        HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        List<Song> songs = new ArrayList<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String query : queries) {
            try {
                URI uri = new URI(ENDPOINT + "?q=" + query + "&type=track&limit=1&offset=0&include_external=audio");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", "Bearer " + spotifyApi.getAccessToken())
                        .GET()
                        .build();

                CompletableFuture<Void> future = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            int statusCode = response.statusCode();
                            if (statusCode >= 200 && statusCode < 300) {
                                String responseBody = response.body();
                                Gson gson = new Gson();
                                SearchTracksAsyncResponse asyncResponse = gson.fromJson(responseBody, SearchTracksAsyncResponse.class);
                                Song song = Song.builder()
                                        .title(asyncResponse.getTracks().getItems().getFirst().getName())
                                        .artist(asyncResponse.getTracks().getItems().getFirst().getArtists().get(0).getName())
                                        .album(asyncResponse.getTracks().getItems().getFirst().getAlbum().getName())
                                        .spotifyId(asyncResponse.getTracks().getItems().getFirst().getUri())
                                        .imageUrl(asyncResponse.getTracks().getItems().getFirst().getAlbum().getImages().getFirst().getUrl())
                                        .duration(String.valueOf(asyncResponse.getTracks().getItems().getFirst().getDurationMs()))
                                        .previewUrl(asyncResponse.getTracks().getItems().getFirst().getPreviewUrl())
                                        .externalUrl(asyncResponse.getTracks().getItems().getFirst().getExternalUrls().getSpotify())
                                        .build();
                                songs.add(song);
                            } else {
                                System.out.println("Search failed for query: " + query + " with status code: " + statusCode);
                            }
                        })
                        .exceptionally(e -> {
                            System.out.println("Error executing search request for query: " + query + ": " + e.getMessage());
                            return null;
                        });
                futures.add(future);
            } catch (URISyntaxException e) {
                System.out.println("Invalid URI for query: " + query + ": " + e.getMessage());
            }
        }
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        return allOf.thenApply(v -> {
            futures.forEach(future -> {
                try {
                    future.get();
                } catch (Exception e) {
                    System.out.println("Error waiting for future completion: " + e.getMessage());
                }
            });
            return songs;
        });
    }
}