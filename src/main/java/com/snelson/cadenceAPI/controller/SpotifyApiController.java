package com.snelson.cadenceAPI.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.snelson.cadenceAPI.model.Song;
import com.snelson.cadenceAPI.service.SpotifyApiService;
import com.snelson.cadenceAPI.utils.CustomGsonExclusionStrategy;
import com.snelson.cadenceAPI.utils.SecureRandomTypeAdapter;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Value("${CLIENT_URL}")
    private String CLIENT_URL;

    @Autowired
    private SpotifyApiService spotifyApiService;

    private String STATE;
    private Gson gson;

    @PostConstruct
    public void init() {
        log.info("Initializing Spotify API");
        log.info("CLIENT_URL: " + CLIENT_URL);

        this.STATE = spotifyApiService.generateRandomString(16);
        this.gson = new GsonBuilder()
                .setExclusionStrategies(new CustomGsonExclusionStrategy())
                .registerTypeAdapter(SecureRandom.class, new SecureRandomTypeAdapter())
                .create();
    }

    @GetMapping("/login/spotify")
    public ResponseEntity<String> loginSpotify() {
        String SCOPE = "playlist-modify-public playlist-modify-private playlist-read-private playlist-read-collaborative user-read-private user-read-email";
        String result = gson.toJson(spotifyApiService.spotifyApi.authorizationCodeUri()
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

            AuthorizationCodeRequest authorizationCodeRequest = spotifyApiService.spotifyApi.authorizationCode(code).build();
            AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            spotifyApiService.spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApiService.spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            spotifyApiService.setCookies(authorizationCodeCredentials.getExpiresIn(), response);

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

            spotifyApiService.checkSpotifyCredentials();

            User user = spotifyApiService.getCurrentUser();
            Playlist newPlaylist = spotifyApiService.createPlaylist(user.getId(), playlist.getName(), playlist.getDescription());
            spotifyApiService.addSongsToPlaylist(newPlaylist.getId(), spotifyApiService.getTrackIds(playlist.getSongs()));

            return new ResponseEntity<>(gson.toJson(newPlaylist), HttpStatus.OK);
        } catch (Exception e) {
            log.severe("Error creating Spotify playlist: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}