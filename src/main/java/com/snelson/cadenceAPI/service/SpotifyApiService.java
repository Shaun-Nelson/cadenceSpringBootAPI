package com.snelson.cadenceAPI.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.snelson.cadenceAPI.dto.SpotifyPlaylistRequestSong;
import com.snelson.cadenceAPI.model.Song;
import com.snelson.cadenceAPI.utils.CustomGsonExclusionStrategy;
import com.snelson.cadenceAPI.utils.SecureRandomTypeAdapter;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
@Log
public class SpotifyApiService {

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
    public SpotifyApi spotifyApi;

    @PostConstruct
    public void init() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setRedirectUri(URI.create(REDIRECT_URI))
                .build();

        log.info("Spotify API initialized");

        this.STATE = generateRandomString(16);
        this.SCOPE = "playlist-modify-public playlist-modify-private playlist-read-private playlist-read-collaborative user-read-private user-read-email";

        this.gson = new GsonBuilder()
                .setExclusionStrategies(new CustomGsonExclusionStrategy())
                .registerTypeAdapter(SecureRandom.class, new SecureRandomTypeAdapter())
                .create();
    }

    public void refreshSync() {
        try {
            AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
            spotifyApi.setAccessToken(authorizationCodeRefreshRequest.execute().getAccessToken());
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