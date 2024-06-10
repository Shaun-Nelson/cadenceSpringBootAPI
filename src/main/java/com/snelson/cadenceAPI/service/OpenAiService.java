package com.snelson.cadenceAPI.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.snelson.cadenceAPI.model.Song;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Service
public class OpenAiService {

    @Value("${OPENAI_API_KEY}")
    private String OPENAI_API_KEY;

    @Autowired
    private SpotifyApiService spotifyApiService;

    public String processRequest(String length, String input) {
        List<ChatMessage> messages = getChatMessages(length, input);
        int TIMEOUT_DURATION_IN_SECONDS = 120;
        com.theokanning.openai.service.OpenAiService service = new com.theokanning.openai.service.OpenAiService(OPENAI_API_KEY, Duration.ofSeconds(TIMEOUT_DURATION_IN_SECONDS));

        try {
            String MODEL = "gpt-4o";
            double TEMPERATURE = 0;
            int MAX_TOKENS = 3500;
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                    .builder()
                    .model(MODEL)
                    .messages(messages)
                    .temperature(TEMPERATURE)
                    .maxTokens(MAX_TOKENS)
                    .build();

            String jsonResponse = service.createChatCompletion(chatCompletionRequest)
                    .getChoices()
                    .getFirst()
                    .getMessage()
                    .getContent();

            spotifyApiService.checkSpotifyCredentials();
            Track[] tracks = getTracksFromJsonAsync(jsonResponse);
            List<Song> songs = getSongsFromTracksNew(tracks);

            return new Gson().toJson(songs);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return new Gson().toJson("Error: " + e.getMessage());
        }
    }

    public String processRequestNew(String length, String input) {
        List<ChatMessage> messages = getChatMessages(length, input);
        int TIMEOUT_DURATION_IN_SECONDS = 120;
        com.theokanning.openai.service.OpenAiService service = new com.theokanning.openai.service.OpenAiService(OPENAI_API_KEY, Duration.ofSeconds(TIMEOUT_DURATION_IN_SECONDS));

        try {
            String MODEL = "gpt-4o";
            double TEMPERATURE = 0;
            int MAX_TOKENS = 3500;
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                    .builder()
                    .model(MODEL)
                    .messages(messages)
                    .temperature(TEMPERATURE)
                    .maxTokens(MAX_TOKENS)
                    .build();

            String jsonResponse = service.createChatCompletion(chatCompletionRequest)
                    .getChoices()
                    .getFirst()
                    .getMessage()
                    .getContent();
            System.out.println(jsonResponse);

            return new Gson().toJson("placeholder songs");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return new Gson().toJson("Error: " + e.getMessage());
        }
    }

    @NotNull
    private List<ChatMessage> getChatMessages(String length, String input) {
        final String PROMPT = "You are an assistant that only responds in JSON format strictly as an array of objects, with no leading or trailing characters!. Create a list of %s unique! songs, found in the Spotify library, based off the following statement: \"%s\". Include \"title\" and \"artist\" in your response. An example response is: [{\"title\": \"Hey Jude\", \"artist\": \"The Beatles\"}].";
        final String PROMPT2 = "You are an assistant that only responds in JSON format, with no leading or trailing characters! Create a list of %s unique! song IDs from songs found in the Spotify library, based off the following search prompt: \"%s\". An example response is: [\"01iyCAUm8EvOFqVWYJ3dVX\"].";
        final String PROMPT3 = "You are an assistant that only responds in JSON format strictly as an array of objects, with no leading or trailing characters!. Create a list of %s unique! songs, found in the Spotify library, based off the following statement: \"%s\". Include \"title\" in your response. An example response is: [{\"title\": \"Hey Jude\"}].";

        String message = String.format(PROMPT, length, input);
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), message);
        messages.add(userMessage);

        return messages;
    }

    @NotNull
    private Track[] getTracksFromJson(String jsonResponse) {
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);
        Track[] tracks = new Track[jsonArray.size()];

        spotifyApiService.checkSpotifyCredentials();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            Track track = spotifyApiService.searchTrack(jsonObject.get("title") + " " + jsonObject.get("artist"));
            tracks[i] = track;
        }
        return tracks;
    }

    private List<Song> getSongsFromTracks(List<Track> tracks) {
        List<Song> songs = new ArrayList<>();
        for (Track track : tracks) {
            songs.add(Song.builder()
                    .spotifyId(track.getUri())
                    .title(track.getName())
                    .artist(track.getArtists()[0].getName())
                    .duration(track.getDurationMs() / 60000 + ":" + (track.getDurationMs() / 1000) % 60)
                    .previewUrl(track.getPreviewUrl())
                    .externalUrl(track.getExternalUrls().getExternalUrls().get("spotify"))
                    .imageUrl(track.getAlbum().getImages()[0].getUrl())
                    .album(track.getAlbum().getName())
                    .build());
        }
        return songs;
    }

    private Track[] getTracksFromJsonSync(String jsonResponse) {
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);
        String[] queries = new String[jsonArray.size()];

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            queries[i] = jsonObject.get("title") + " " + jsonObject.get("artist");
        }
        return spotifyApiService.getTracksSync(queries);
    }

    private Track[] getTracksFromJsonAsync(String jsonResponse) {
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);
        Track[] tracks = new Track[jsonArray.size()];

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String query = jsonObject.get("title") + " " + jsonObject.get("artist");
            CompletableFuture<Paging<Track>> pagingFuture = spotifyApiService.getTrackAsync(query);
            try {
                Paging<Track> paging = pagingFuture.get();
                tracks[i] = paging.getItems()[0];
            } catch (Exception e) {
                System.out.println("Error getting track for query '" + query + "': " + e.getMessage());
            }
        }
        return tracks;
    }

    private List<Song> getSongsFromTracksNew(Track[] tracks) {
        Set<Track> trackSet = new HashSet<>(Arrays.asList(tracks));
        List<Song> songs = new ArrayList<>();
        for (Track track : trackSet) {
            songs.add(Song.builder()
                    .spotifyId(track.getUri())
                    .title(track.getName())
                    .artist(track.getArtists()[0].getName())
                    .duration(track.getDurationMs() / 60000 + ":" + (track.getDurationMs() / 1000) % 60)
                    .previewUrl(track.getPreviewUrl())
                    .externalUrl(track.getExternalUrls().getExternalUrls().get("spotify"))
                    .imageUrl(track.getAlbum().getImages()[0].getUrl())
                    .album(track.getAlbum().getName())
                    .build());
        }
        return songs;
    }
}
