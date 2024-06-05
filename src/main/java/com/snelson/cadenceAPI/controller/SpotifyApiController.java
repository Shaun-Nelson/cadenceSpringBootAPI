package com.snelson.cadenceAPI.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.snelson.cadenceAPI.model.Song;
import com.snelson.cadenceAPI.utils.CustomGsonExclusionStrategy;
import com.snelson.cadenceAPI.utils.SecureRandomTypeAdapter;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Log
@RestController
@RequestMapping("/api")
public class SpotifyApiController {

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

    private String STATE;
    private String SCOPE;
    private Gson gson;
    public static SpotifyApi spotifyApi;

    @PostConstruct
    public void init() {
        log.info("CLIENT_ID: " + CLIENT_ID);
        log.info("CLIENT_SECRET: " + CLIENT_SECRET);
        log.info("REDIRECT_URI: " + REDIRECT_URI);
        log.info("CLIENT_URL: " + CLIENT_URL);
        log.info("ENV: " + ENV);

        spotifyApi = new SpotifyApi.Builder()
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setRedirectUri(URI.create(REDIRECT_URI))
                .build();

        log.info("Spotify API initialized: " + this.spotifyApi.toString());

        this.STATE = generateRandomString(16);
        this.SCOPE = "playlist-modify-public playlist-modify-private playlist-read-private playlist-read-collaborative user-read-private user-read-email";

        this.gson = new GsonBuilder()
                .setExclusionStrategies(new CustomGsonExclusionStrategy())
                .registerTypeAdapter(SecureRandom.class, new SecureRandomTypeAdapter())
                .create();
    }

    @GetMapping("/login/spotify")
    public ResponseEntity<String> loginSpotify() {
        String result = gson.toJson(spotifyApi.authorizationCodeUri()
                .scope(SCOPE)
                .state(STATE)
                .show_dialog(true)
                .build()
                .execute()
                .toString());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/callback")
    public RedirectView authorizationCodeSync(@RequestParam String code, @RequestParam String state, HttpServletResponse response) {
        try {
            if (!state.equals(this.STATE)) {
                throw new Exception("State mismatch");
            }

            AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
            AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            setCookies(authorizationCodeCredentials.getExpiresIn(), response);

            return new RedirectView(CLIENT_URL);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.severe("Error on Spotify callback: " + e.getMessage());
            return new RedirectView(CLIENT_URL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/playlists/spotify")
    public ResponseEntity<String> createSpotifyPlaylist(@Valid @RequestBody String playlistJson) {
        try {
            com.snelson.cadenceAPI.model.Playlist playlist = gson.fromJson(playlistJson, com.snelson.cadenceAPI.model.Playlist.class);

            checkSpotifyCredentials();

            User user = getCurrentUser();
            Playlist newPlaylist = createPlaylist(user.getId(), playlist.getName(), playlist.getDescription());
            addSongsToPlaylist(newPlaylist.getId(), getTrackIds(playlist.getSongs()));

            return new ResponseEntity<>(gson.toJson(newPlaylist), HttpStatus.OK);
        } catch (Exception e) {
            log.severe("Error creating Spotify playlist: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static void refreshSync() {
        try {
            AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
            spotifyApi.setAccessToken(authorizationCodeRefreshRequest.execute().getAccessToken());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error refreshing Spotify access token: " + e.getMessage());
        }
    }

    private static void clientCredentials_Sync() {
        try {
            final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
            spotifyApi.setAccessToken(clientCredentialsRequest.execute().getAccessToken());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error getting Spotify access token: " + e.getMessage());
        }
    }

    private User getCurrentUser() throws IOException, SpotifyWebApiException, ParseException {
        GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = spotifyApi.getCurrentUsersProfile().build();
        return getCurrentUsersProfileRequest.execute();
    }

    private Playlist createPlaylist(String userId, String playlistName, String description) throws IOException, SpotifyWebApiException, ParseException {
        CreatePlaylistRequest createPlaylistRequest = spotifyApi.createPlaylist(userId, playlistName).description(description).build();
        return createPlaylistRequest.execute();
    }

    private void addSongsToPlaylist(String playlistId, String[] trackIds) throws IOException, ParseException, SpotifyWebApiException {
        spotifyApi.addItemsToPlaylist(playlistId, trackIds).build().execute();
    }

    public static void checkSpotifyCredentials() {
        if (spotifyApi.getAccessToken() == null) {
            if (spotifyApi.getRefreshToken() != null) {
                refreshSync();
            } else {
                clientCredentials_Sync();
            }
        }
    }

    private String[] getTrackIds(List<Song> songs) {
        List<String> trackIds = new ArrayList<>();
        for (Song song : songs) {
            trackIds.add(song.getSpotifyId());
        }
        return trackIds.toArray(new String[0]);
    }

    public static Track searchTrack(String query) {
        try {
            checkSpotifyCredentials();
            return spotifyApi.searchItem(query, "track").build().execute().getTracks().getItems()[0];
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error searching track: " + e.getMessage());
            return null;
        }
    }

    public static List<Track> getSpotifySongs(String[] trackIds) {
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

    public static String generateRandomString(int length) {
        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        while (length-- != 0) {
            int character = (int) (Math.random() * alphaNumericString.length());
            builder.append(alphaNumericString.charAt(character));
        }
        return builder.toString();
    }

    private void setCookies(int accessTokenExpiresIn, HttpServletResponse response) {
        try {
            int EXPIRES_IN = 60 * 60 * 24 * 30;

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
        } catch (Exception e) {
            System.out.println("Error setting cookies: " + e.getMessage());
        }
    }
}