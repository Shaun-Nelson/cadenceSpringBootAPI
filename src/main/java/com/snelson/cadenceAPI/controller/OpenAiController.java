package com.snelson.cadenceAPI.controller;

import com.google.gson.*;
import com.snelson.cadenceAPI.model.Song;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.time.Duration;
import java.util.*;

import static com.snelson.cadenceAPI.controller.SpotifyApiController.*;

@RestController
@RequestMapping("/api/openai")
public class OpenAiController {

    @Value("${OPENAI_API_KEY}")
    private String OPENAI_API_KEY;

    private static final String MODEL = "gpt-4o";
    private static final double TEMPERATURE = 0;
    private static final int MAX_TOKENS = 3500;
    private static final int TIMEOUT_DURATION_IN_SECONDS = 120;
    private static final String PROMPT = "You are an assistant that only responds in JSON format strictly as an array of objects, with no leading or trailing characters!. Create a list of %s unique! songs, found in the Spotify library, based off the following statement: \"%s\". Include \"id\", \"title\", \"artist\", \"album\", and \"duration\" in your response. An example response is: [{\"id\": 1,\"title\": \"Hey Jude\", \"artist\": \"The Beatles\",\"album\": \"The Beatles (White Album)\",\"duration\": \"4:56\"}].";
    private static final String PROMPT2 = "Create an array of %s unique! songs, queried from the Spotify library, based off the following search prompt: \"%s\". Please provide a list of Spotify track IDs in a JSON array format, with no leading or trailing characters!.";
    private final SpotifyApiController spotifyApiController = new SpotifyApiController();

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
        OpenAiService service = new OpenAiService(OPENAI_API_KEY, Duration.ofSeconds(TIMEOUT_DURATION_IN_SECONDS));
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

            Track[] trackIds = getTrackIdsFromJson(jsonResponse);
            List<Song> songs = spotifyApiController.getSpotifySongs(trackIds);


            return new Gson().toJson(songs);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    @NotNull
    private List<ChatMessage> getChatMessages(String length, String input) {
        String message = String.format(PROMPT, length, input);
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), message);
        messages.add(userMessage);

        return messages;
    }

    @NotNull
    private Track[] getTrackIdsFromJson(String jsonResponse) {
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);
        List<Track> responseList = new ArrayList<>();

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            Track track = spotifyApiController.searchTrack(jsonObject.get("title").getAsString() + " " + jsonObject.get("artist").getAsString());
            if (track != null) {
                responseList.add(track);
            }
        }
        return responseList.toArray(new Track[0]);
    }
}

