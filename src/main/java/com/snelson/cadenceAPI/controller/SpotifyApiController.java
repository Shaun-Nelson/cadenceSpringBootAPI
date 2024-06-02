package com.snelson.cadenceAPI.controller;

import com.google.gson.Gson;
import com.snelson.cadenceAPI.model.Playlist;
import com.snelson.cadenceAPI.model.Song;
import com.snelson.cadenceAPI.service.PlaylistService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
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
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SpotifyApiController {

    private final String CLIENT_URL = System.getenv("CLIENT_URL");
    private static final String ENV = System.getenv("ENV");
    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(System.getenv("CLIENT_ID"))
            .setClientSecret(System.getenv("CLIENT_SECRET"))
            .setRedirectUri(SpotifyHttpManager.makeUri(System.getenv("REDIRECT_URI")))
            .build();
    private final String STATE = generateRandomString(16);
    private final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
            .scope("playlist-modify-public playlist-modify-private playlist-read-private playlist-read-collaborative user-read-private user-read-email")
            .state(STATE)
            .show_dialog(true)
            .build();
    private static final AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
    private static final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
    private static final int EXPIRES_IN = 60 * 60 * 24 * 30;

    @GetMapping("/login/spotify")
    public String authorizationCodeUriSync() {
        try {
            Gson gson = new Gson();
            return gson.toJson(authorizationCodeUriRequest.execute());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return CLIENT_URL;
        }
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
            System.out.println("Playlist JSON: " + playlistJson);
            Gson gson = new Gson();
            Playlist newPlaylist = gson.fromJson(playlistJson, Playlist.class);
            System.out.println("New playlist: " + newPlaylist);

            if (newPlaylist == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            System.out.println("Creating playlist: " + newPlaylist.getName());
            String newPlaylistLink = createSpotifyPlaylist(newPlaylist);
            System.out.println("Playlist created: " + newPlaylist.getName());

            return new ResponseEntity<>(newPlaylistLink, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static void authorizationCodeRefreshSync() {
        try {
            AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());

            System.out.println("Expires in: " + authorizationCodeCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void clientCredentials_Sync() {
        try {
            final ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            System.out.println("Expires in: " + clientCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private String createSpotifyPlaylist(Playlist newPlaylist) throws IOException, ParseException, SpotifyWebApiException {
        checkSpotifyCredentials(spotifyApi.getAccessToken(), spotifyApi.getRefreshToken());

        String name = newPlaylist.getName();
        String description = newPlaylist.getDescription();
        List<Song> songs = newPlaylist.getSongs();

        try {
            String userId = spotifyApi.getCurrentUsersProfile().build().execute().getId();

            se.michaelthelin.spotify.model_objects.specification.Playlist playlist = spotifyApi.createPlaylist(userId, name)
                    .description(description)
                    .public_(false)
                    .collaborative(false)
                    .build().execute();

            String[] uris = new String[songs.size()];
            for (int i = 0; i < songs.size(); i++) {
                uris[i] = songs.get(i).getExternalUrl();
            }

            spotifyApi.addItemsToPlaylist(playlist.getId(), uris).build().execute();

            return playlist.getExternalUrls().getExternalUrls().get("spotify");
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.out.println("Error creating playlist: " + e.getMessage());
                return null;
            }
    }

    public static Track searchTrack(String query) {
        checkSpotifyCredentials(spotifyApi.getAccessToken(), spotifyApi.getRefreshToken());

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

    public static List<Song> getSpotifySongs(Track[] trackIds) {
        checkSpotifyCredentials(spotifyApi.getAccessToken(), spotifyApi.getRefreshToken());

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

    @NotNull
    private static List<Song> getSongsWithMetadata(@NotNull Track[] tracks) {
        checkSpotifyCredentials(spotifyApi.getAccessToken(), spotifyApi.getRefreshToken());

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

    private static void checkSpotifyCredentials(@CookieValue(value = "access_token", required = false) String accessToken, @CookieValue(value = "refresh_token", required = false) String refreshToken) {
        if (accessToken == null && refreshToken == null) {
            clientCredentials_Sync();
        } else if (accessToken != null) {
            spotifyApi.setAccessToken(accessToken);
        } else {
            authorizationCodeRefreshSync();
        }
    }
}
