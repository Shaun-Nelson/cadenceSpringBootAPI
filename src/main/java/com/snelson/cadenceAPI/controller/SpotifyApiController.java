package com.snelson.cadenceAPI.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.snelson.cadenceAPI.dto.SpotifyPlaylistRequest;
import com.snelson.cadenceAPI.model.RefreshToken;
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
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;

import java.io.IOException;
import java.security.SecureRandom;
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
        this.STATE = spotifyApiService.generateRandomString(16);
        this.gson = new GsonBuilder()
                .setExclusionStrategies(new CustomGsonExclusionStrategy())
                .registerTypeAdapter(SecureRandom.class, new SecureRandomTypeAdapter())
                .create();
        log.info("SpotifyApiController initialized");
    }

    @GetMapping("/login/spotify")
    public ResponseEntity<String> loginSpotify() {
        try {
        String SCOPE = "playlist-modify-public playlist-modify-private playlist-read-private playlist-read-collaborative user-read-private user-read-email streaming user-read-playback-state user-modify-playback-state";
        String result = gson.toJson(spotifyApiService.spotifyApi.authorizationCodeUri()
                .scope(SCOPE)
                .state(STATE)
                .show_dialog(true)
                .build()
                .execute()
                .toString());

        return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.severe("Error on Spotify login: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
    public ResponseEntity<String> createSpotifyPlaylist(@CookieValue String refresh_token, @Valid @RequestBody String playlistRequest) {
        try {
            spotifyApiService.spotifyApi.setRefreshToken(refresh_token);
            spotifyApiService.refreshSync();
            Gson gson = new Gson();
            SpotifyPlaylistRequest request = gson.fromJson(playlistRequest, SpotifyPlaylistRequest.class);

            User user = spotifyApiService.getCurrentUser();
            Playlist newPlaylist = spotifyApiService.createPlaylist(user.getId(), request.getName(), request.getDescription());
            spotifyApiService.addSongsToPlaylist(newPlaylist.getId(), spotifyApiService.getTrackIds(request.getSongs()));

            return new ResponseEntity<>(gson.toJson(newPlaylist), HttpStatus.OK);
        } catch (Exception e) {
            log.severe("Error creating Spotify playlist: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/login/spotify/refresh")
    public ResponseEntity<String> refreshSpotifyToken(@CookieValue String refresh_token, HttpServletResponse response) {
        try {
            if (!refresh_token.isEmpty()) {
                spotifyApiService.spotifyApi.setRefreshToken(refresh_token);
                spotifyApiService.refreshSync();
                spotifyApiService.setCookies(3600, response);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.severe("Error refreshing Spotify token: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}