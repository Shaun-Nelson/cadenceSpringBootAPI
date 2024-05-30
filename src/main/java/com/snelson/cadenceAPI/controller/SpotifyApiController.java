package com.snelson.cadenceAPI.controller;

import com.snelson.cadenceAPI.model.Song;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.special.SearchResult;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.search.SearchItemRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetSeveralTracksRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SpotifyApiController {

    @Value("${CLIENT_ID}")
    private String clientId;

    @Value("${CLIENT_SECRET}")
    private String clientSecret;

    @Value("${REDIRECT_URI}")
    private String redirectUri;

    @Value("${CLIENT_URL}")
    private String clientUrl;

    @Value("${ENV}")
    private String env;

    private SpotifyApi spotifyApi;
    private final String state;
    private AuthorizationCodeUriRequest authorizationCodeUriRequest;
    private AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest;
    private ClientCredentialsRequest clientCredentialsRequest;
    private static final int EXPIRES_IN = 60 * 60 * 24 * 30;

    public SpotifyApiController() {
        this.state = generateRandomString(16);
    }

    @GetMapping("/api/login/spotify")
    public void authorizationCodeUriSync(HttpServletResponse response) {
        try {
            initSpotifyApi();
            response.sendRedirect(String.valueOf(authorizationCodeUriRequest.execute()));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @GetMapping("/api/callback")
    public void authorizationCodeSync(@RequestParam String code, @RequestParam String state, HttpServletResponse response) {
        try {
            if (!state.equals(this.state)) {
                System.out.println("State mismatch");
                return;
            }

            AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
            AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            setCookies(authorizationCodeCredentials.getExpiresIn(), response);

            response.sendRedirect(clientUrl);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void authorizationCodeRefreshSync(HttpServletResponse response) {
        try {
            initSpotifyApi();
            AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            setCookies(authorizationCodeCredentials.getExpiresIn(), response);

            System.out.println("Expires in: " + authorizationCodeCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void clientCredentialsSync() {
        try {
            initSpotifyApi();
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            System.out.println("Expires in: " + clientCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public Track searchTrack(String query) {
        checkSpotifyCredentials();

        SearchItemRequest searchItemRequest = spotifyApi.searchItem(query, ModelObjectType.TRACK.getType())
                .includeExternal("audio")
                .build();

        try {
            SearchResult searchResult = searchItemRequest.execute();
            return searchResult.getTracks().getItems()[0];
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public List<Song> getSpotifySongs(Track[] trackIds) {
        checkSpotifyCredentials();

        String[] ids = new String[trackIds.length];
        for (int i = 0; i < trackIds.length; i++) {
            ids[i] = trackIds[i].getId();
        }

        GetSeveralTracksRequest getSeveralTracksRequest = spotifyApi.getSeveralTracks(ids).build();

        try {
            Track[] tracks = getSeveralTracksRequest.execute();
            return getSongsWithMetadata(tracks);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    @NotNull
    private String generateRandomString(int length) {
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
            Cookie accessToken = new Cookie("access_token", spotifyApi.getAccessToken());
            accessToken.setMaxAge(accessTokenExpiresIn);
            accessToken.setSecure(env.equals("production"));
            accessToken.setPath("/");
            response.addCookie(accessToken);

            Cookie refreshToken = new Cookie("refresh_token", spotifyApi.getRefreshToken());
            refreshToken.setMaxAge(EXPIRES_IN);
            refreshToken.setSecure(env.equals("production"));
            refreshToken.setPath("/");
            response.addCookie(refreshToken);
        } catch (Exception e) {
            System.out.println("Error setting cookies: " + e.getMessage());
        }
    }

    @NotNull
    private List<Song> getSongsWithMetadata(@NotNull Track[] tracks) {
        checkSpotifyCredentials();

        List<Song> songs = new ArrayList<>();

        for (Track track : tracks) {
            Song song = Song.builder()
                    .title(track.getName())
                    .artist(track.getArtists()[0].getName())
                    .duration(track.getDurationMs() / 60000 + ":" + (track.getDurationMs() / 1000) % 60)
                    .previewUrl(track.getPreviewUrl())
                    .externalUrl(track.getExternalUrls().getExternalUrls().get("spotify"))
                    .imageUrl(track.getAlbum().getImages()[0].getUrl())
                    .album(track.getAlbum().getName())
                    .build();

            songs.add(song);
        }
        return songs;
    }

    private void checkSpotifyCredentials() {
        if (spotifyApi.getAccessToken() == null || spotifyApi.getAccessToken().isEmpty()) {
            clientCredentialsSync();
        }
    }

    private void initSpotifyApi() {
        if (spotifyApi == null) {
            spotifyApi = new SpotifyApi.Builder()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRedirectUri(SpotifyHttpManager.makeUri(redirectUri))
                    .build();

            authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                    .scope("playlist-modify-public playlist-modify-private playlist-read-private playlist-read-collaborative")
                    .state(state)
                    .show_dialog(true)
                    .build();

            authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
            clientCredentialsRequest = spotifyApi.clientCredentials().build();
        }
    }
}
