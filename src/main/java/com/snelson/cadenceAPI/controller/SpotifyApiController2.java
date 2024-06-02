package com.snelson.cadenceAPI.controller;

import com.google.gson.Gson;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class SpotifyApiController2 {

    private static final String CLIENT_ID = System.getenv("CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("CLIENT_SECRET");
    private static final String REDIRECT_URI = System.getenv("REDIRECT_URI");
    private static final String CLIENT_URL = System.getenv("CLIENT_URL");
    private static final String ENV = System.getenv("ENV");
    private static final int EXPIRES_IN = 60 * 60 * 24 * 30;
    private final String STATE = generateRandomString(16);
    private static String accessToken;
    private static String refreshToken;

    @GetMapping("/login/spotify")
    public ResponseEntity<String> loginSpotify() {
        String SCOPE = "playlist-modify-public playlist-modify-private playlist-read-private playlist-read-collaborative user-read-private user-read-email";
        String uri = "https://accounts.spotify.com/authorize" +
                "?response_type=code" +
                "&client_id=" + CLIENT_ID +
                "&scope=" + SCOPE +
                "&redirect_uri=" + REDIRECT_URI +
                "&state=" + STATE;

        return ResponseEntity.status(HttpStatus.OK).body(new Gson().toJson(uri));
    }

    @GetMapping("/callback")
    public RedirectView authorizationCodeSync(@RequestParam String code, @RequestParam String state, HttpServletResponse response) {
        if (!state.equals(this.STATE)) {
            System.out.println("State mismatch");
            return new RedirectView(CLIENT_URL + "/error");
        }

        String result = "https://accounts.spotify.com/api/token" +
                "?grant_type=authorization_code" +
                "&code=" + code +
                "&redirect_uri=" + REDIRECT_URI +
                "&client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(result))
                    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            setCookies(httpResponse.body(), response);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return new RedirectView(CLIENT_URL);
    }

    public static String getSpotifySongs(String[] trackIds) {
        checkSpotifyCredentials(accessToken, refreshToken);
        String uri = "https://api.spotify.com/v1/tracks?ids=" + String.join(",", trackIds);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            return httpResponse.body();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String generateRandomString(int length) {
        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        while (length-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    private static void setCookies(String body, HttpServletResponse response) {
        String accessToken = body.split("\"access_token\":\"")[1].split("\"")[0];
        String refreshToken = body.split("\"refresh_token\":\"")[1].split("\"")[0];
        int accessTokenExpiresIn = Integer.parseInt(body.split("\"expires_in\":")[1].split(",")[0]);

        try {
            Cookie accessTokenCookie = new Cookie("access_token", accessToken);
            accessTokenCookie.setMaxAge(accessTokenExpiresIn);
            accessTokenCookie.setSecure(ENV.equals("production"));
            accessTokenCookie.setPath("/");
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
            refreshTokenCookie.setMaxAge(EXPIRES_IN);
            refreshTokenCookie.setSecure(ENV.equals("production"));
            refreshTokenCookie.setPath("/");
            response.addCookie(refreshTokenCookie);
        } catch (Exception e) {
            System.out.println("Error setting cookies: " + e.getMessage());
        }
    }

    private static String getNewAccessToken(@CookieValue(value = "refresh_token", required = false) String refresh) {
        String uri = "https://accounts.spotify.com/api/token" +
                "?grant_type=refresh_token" +
                "&refresh_token=" + refresh +
                "&client_id=" + CLIENT_ID;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            String accessToken = httpResponse.body().split("\"access_token\":\"")[1].split("\"")[0];
            String refreshToken = httpResponse.body().split("\"refresh_token\":\"")[1].split("\"")[0];

            return accessToken + " " + refreshToken;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private static void checkSpotifyCredentials(@CookieValue(value = "access_token", required = false) String accessToken, @CookieValue(value = "refresh_token") String refreshToken) {
        if ((accessToken == null || accessToken.isEmpty()) && (refreshToken == null || refreshToken.isEmpty())) {
            System.out.println("No access token or refresh token");
        } else if (accessToken == null || accessToken.isEmpty()) {
            getNewAccessToken(refreshToken);
        }
    }
}
