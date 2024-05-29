package com.snelson.cadenceAPI.controller;

import com.google.gson.*;
import com.snelson.cadenceAPI.model.Song;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.snelson.cadenceAPI.controller.SpotifyApiController.*;

@RestController
@RequestMapping("/api/openai")
public class OpenAiController {

    private static final String MODEL = "gpt-4o";
    private static final double TEMPERATURE = 0;
    private static final int MAX_TOKENS = 3500;
    private static final int TIMEOUT_DURATION_IN_SECONDS = 120;
    private static final String PROMPT = "You are an assistant that only responds in JSON format strictly as an array of objects. Create a list of %s unique songs, found in the Spotify library, based off the following statement: \"%s\". Be sure to check that there are no duplicates. Include \"id\", \"title\", \"artist\", \"album\", and \"duration\" in your response. An example response is: [{\"id\": 1,\"title\": \"Hey Jude\", \"artist\": \"The Beatles\",\"album\": \"The Beatles (White Album)\",\"duration\": \"4:56\"}].";

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getOpenAiResponseFromForm(@RequestBody MultiValueMap<String, String> formBody) {
        String length = formBody.getFirst("length");
        String input = formBody.getFirst("input");
        return processRequest(length, input);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getOpenAiResponseFromJson(@RequestBody String jsonBody) {
        JsonObject jsonObject = JsonParser.parseString(jsonBody).getAsJsonObject();
        String length = jsonObject.get("length").getAsString();
        String input = jsonObject.get("input").getAsString();
        return processRequest(length, input);
    }

    private String processRequest(String length, String input) {
        List<ChatMessage> messages = getChatMessages(length, input);
        OpenAiService service = new OpenAiService(System.getenv("OPENAI_API_KEY"), Duration.ofSeconds(TIMEOUT_DURATION_IN_SECONDS));
        try {
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                    .builder()
                    .model(MODEL)
                    .messages(messages)
                    .temperature(TEMPERATURE)
                    .maxTokens(MAX_TOKENS)
                    .build();

            String jsonResponse = service.createChatCompletion(chatCompletionRequest)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();

            List<Song> songs = getSongObjectsFromJson(jsonResponse);
            List<Song> results = getSpotifySongs(songs);

            Gson gson = new Gson();
            return gson.toJson(results);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    @NotNull
    private static List<ChatMessage> getChatMessages(String length, String input) {
        String message = String.format(PROMPT, length, input);
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), message);
        messages.add(userMessage);

        return messages;
    }

    @NotNull
    public static List<Song> getSongObjectsFromJson(String jsonResponse) {
        List<Song> songs = new ArrayList<>();
        JsonArray jsonArray = JsonParser.parseString(jsonResponse).getAsJsonArray();

        for (JsonElement element : jsonArray) {
            JsonObject songObject = element.getAsJsonObject();
            Song song = new Song();
            song.setId(songObject.get("id").getAsInt());
            song.setTitle(songObject.get("title").getAsString());
            song.setArtist(songObject.get("artist").getAsString());
            song.setAlbum(songObject.get("album").getAsString());
            song.setDuration(songObject.get("duration").getAsString());
            songs.add(song);
        }
        return songs;
    }
}
