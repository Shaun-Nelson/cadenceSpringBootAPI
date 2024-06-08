package com.snelson.cadenceAPI.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.snelson.cadenceAPI.model.Song;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


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
            Track[] tracks = getTracksFromJson(jsonResponse);
            Song[] songs = getSongsFromTracksNew(tracks);

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

    @NotNull
    private String[] getTrackIdsFromJsonNew(String jsonResponse) {
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);
        List<String> trackIds = new ArrayList<>();

        for (JsonElement element : jsonArray) {
            if (element.getAsString().startsWith("spotify:track:")) {
                trackIds.add(element.getAsString().substring(14));
            } else {
                trackIds.add(element.getAsString());
            }
        }

        return trackIds.toArray(new String[0]);
    }

    private Song[] getSongsFromTracksNew(Track[] tracks) {
        Song[] songs = new Song[tracks.length];
        for (int i = 0; i < tracks.length; i++) {
            songs[i] = Song.builder()
                    .spotifyId(tracks[i].getUri())
                    .title(tracks[i].getName())
                    .artist(tracks[i].getArtists()[0].getName())
                    .duration(tracks[i].getDurationMs() / 60000 + ":" + (tracks[i].getDurationMs() / 1000) % 60)
                    .previewUrl(tracks[i].getPreviewUrl())
                    .externalUrl(tracks[i].getExternalUrls().getExternalUrls().get("spotify"))
                    .imageUrl(tracks[i].getAlbum().getImages()[0].getUrl())
                    .album(tracks[i].getAlbum().getName())
                    .build();
        }
        return songs;
    }
}
