package com.snelson.cadenceAPI.controller;

import com.snelson.cadenceAPI.model.Song;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Response;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.special.SearchResult;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.search.SearchItemRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetSeveralTracksRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SpotifyApiController {

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(System.getenv("CLIENT_ID"))
            .setClientSecret(System.getenv("CLIENT_SECRET"))
            .setRedirectUri(SpotifyHttpManager.makeUri(System.getenv("REDIRECT_URI")))
            .build();
    private static final String scope = "playlist-modify-public playlist-modify-private playlist-read-private playlist-read-collaborative";
    private static final String state = generateRandomString(16);
    private static final String code = "";
    private static final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
            .scope(scope)
            .state(state)
            .show_dialog(true)
            .build();
    private static final AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
    private static final AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
            .build();
    private static final int EXPIRES_IN = 60 * 60 * 24 * 30;
    private static final HttpServletResponse response = new Response();

    @GetMapping("/api/login/spotify")
    public static void authorizationCodeUri_Sync(HttpServletResponse response) {
        try {
            response.sendRedirect(String.valueOf(authorizationCodeUriRequest.execute()));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @GetMapping("/api/callback")
    public static void authorizationCode_Sync(HttpServletResponse response) {
        try {
            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            setCookies(authorizationCodeCredentials.getExpiresIn());

            System.out.println("Expires in: " + authorizationCodeCredentials.getExpiresIn());

            response.sendRedirect(System.getenv("CLIENT_URL"));
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void authorizationCodeRefresh_Sync() {
        try {
            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            setCookies(authorizationCodeCredentials.getExpiresIn());

            System.out.println("Expires in: " + authorizationCodeCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static List<Song> getSpotifySongs(List<Song> songs) throws IOException {
        if (spotifyApi.getRefreshToken() == null) {
            authorizationCode_Sync(response);
        } else {
            authorizationCodeRefresh_Sync();
        }

        String[] ids = new String[songs.size()];

        for (Song song : songs) {
            Paging<Track> track = searchTrack(song.getTitle());
            assert track != null;
            song.setSpotifyId(track.getItems()[0].getId());

            ids[songs.indexOf(song)] = song.getSpotifyId();
        }

        GetSeveralTracksRequest getSeveralTracksRequest = spotifyApi.getSeveralTracks(ids)
                .build();

        try {
            final Track[] tracks = getSeveralTracksRequest.execute();

            return getSongsWithMetadata(tracks);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    @NotNull
    public static String generateRandomString(int length) {
        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        while (length-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static void setCookies(Integer accessTokenExpiresIn) {
        try {
        ResponseCookie accessToken = ResponseCookie.from("access_token", spotifyApi.getAccessToken())
                .maxAge(accessTokenExpiresIn)
                .secure(true)
                .sameSite(System.getenv("ENV").equals("DEV") ? "lax" : "none")
                .path("/")
                .build();

        ResponseCookie refreshToken = ResponseCookie.from("refresh_token", spotifyApi.getRefreshToken())
                .maxAge(EXPIRES_IN)
                .secure(true)
                .sameSite(System.getenv("ENV").equals("DEV") ? "lax" : "none")
                .path("/")
                .build();

        ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessToken.toString(), refreshToken.toString())
                .build();
        } catch (Exception e) {
            System.out.println("Error setting cookies: " + e.getMessage());
        }
    }

    public static Paging<Track> searchTrack(String query) {
        SearchItemRequest searchItemRequest = spotifyApi.searchItem(query, ModelObjectType.TRACK.getType())
                .includeExternal("audio")
                .build();

        try {
            SearchResult searchResult = searchItemRequest.execute();
            return searchResult.getTracks();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    @NotNull
    public static List<Song> getSongsWithMetadata(@NotNull Track[] tracks) {
        List<Song> songs = new ArrayList<Song>();

        for (Track track : tracks) {
            Song song = Song.builder()
                    .title(track.getName())
                    .artist(track.getArtists()[0].getName())
                    .duration(track.getDurationMs())
                    .previewUrl(track.getPreviewUrl())
                    .externalUrl(track.getExternalUrls().getExternalUrls().get("spotify"))
                    .imageUrl(track.getAlbum().getImages()[0].getUrl())
                    .album(track.getAlbum().getName())
                    .build();

            songs.add(song);
        }
        return songs;
    }
}