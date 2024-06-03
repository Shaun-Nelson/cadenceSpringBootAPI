package com.snelson.cadenceAPI.controller;

import com.google.gson.Gson;
import com.snelson.cadenceAPI.model.Song;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.hc.core5.http.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SpotifyApiController {

    private static final String CLIENT_ID = System.getenv("CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("CLIENT_SECRET");
    private static final String REDIRECT_URI = System.getenv("REDIRECT_URI");
    private static final String CLIENT_URL = System.getenv("CLIENT_URL");
    private static final String ENV = System.getenv("ENV");
    private static final int EXPIRES_IN = 60 * 60 * 24 * 30;
    private final String STATE = generateRandomString(16);

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(CLIENT_ID)
            .setClientSecret(CLIENT_SECRET)
            .setRedirectUri(URI.create(REDIRECT_URI))
            .build();

    private static AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest;

    @GetMapping("/login/spotify")
    public ResponseEntity<String> loginSpotify() {
        String result = new Gson().toJson(spotifyApi.authorizationCodeUri()
                .scope("playlist-modify-public playlist-modify-private playlist-read-private playlist-read-collaborative user-read-private user-read-email")
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
                System.out.println("State mismatch");
                return new RedirectView(CLIENT_URL);
            }

            AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
            AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            setCookies(authorizationCodeCredentials.getExpiresIn(), response);

            return new RedirectView(CLIENT_URL);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return new RedirectView(CLIENT_URL);
        }
    }

    @PostMapping("/playlists/spotify")
    public ResponseEntity<String> createSpotifyPlaylist(@Valid @RequestBody String playlistJson) {
        try {
            com.snelson.cadenceAPI.model.Playlist playlist = new Gson().fromJson(playlistJson, com.snelson.cadenceAPI.model.Playlist.class);

            checkSpotifyCredentials();

            User user = getCurrentUser();
            Playlist newPlaylist = createPlaylist(user.getId(), playlist.getName(), playlist.getDescription());

            addSongsToPlaylist(newPlaylist.getId(), getTrackIds(playlist.getSongs()));

            return new ResponseEntity<>(new Gson().toJson(newPlaylist), HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static void refreshSync() {
        try {
            AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
            spotifyApi.setAccessToken(authorizationCodeRefreshRequest.execute().getAccessToken());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void clientCredentials_Sync() {
        try {
            final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
            spotifyApi.setAccessToken(clientCredentialsRequest.execute().getAccessToken());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static User getCurrentUser() throws IOException, SpotifyWebApiException, ParseException {
        GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = spotifyApi.getCurrentUsersProfile().build();
        return getCurrentUsersProfileRequest.execute();
    }

    private static Playlist createPlaylist(String userId, String playlistName, String description) throws IOException, SpotifyWebApiException, ParseException {
        CreatePlaylistRequest createPlaylistRequest = spotifyApi.createPlaylist(userId, playlistName).description(description).build();
        return createPlaylistRequest.execute();
    }

    private static void addSongsToPlaylist(String playlistId, String[] trackIds) throws IOException, ParseException, SpotifyWebApiException {
        spotifyApi.addItemsToPlaylist(playlistId, trackIds).build().execute();
    }

    private static void checkSpotifyCredentials() {
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

    public static List<Track> getSpotifySongs(String[] trackIds) {
        checkSpotifyCredentials();

        List<Track> tracks = new ArrayList<>();

        for (String trackId : trackIds) {
            try {
                tracks.add(spotifyApi.getTrack(trackId).build().execute());
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return tracks;
    }

    public static Track searchTrack(String query) {
        try {
            checkSpotifyCredentials();
            return spotifyApi.searchItem(query, "track").build().execute().getTracks().getItems()[0];
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String generateRandomString(int length) {
        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        while (length-- != 0) {
            int character = (int) (Math.random() * alphaNumericString.length());
            builder.append(alphaNumericString.charAt(character));
        }
        return builder.toString();
    }

    private static void setCookies(int accessTokenExpiresIn, HttpServletResponse response) {
        try {
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