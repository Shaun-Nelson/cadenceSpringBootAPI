package com.snelson.cadenceAPI.controller;

import com.google.gson.*;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/openai")
public class OpenAiController {

    private static final String MODEL = "gpt-4o";
    private static final double TEMPERATURE = 0;
    private static final int MAX_TOKENS = 3500;

    @PostMapping(consumes = "application/x-www-form-urlencoded", produces = "application/json")
    public String getOpenAiResponse(@RequestBody MultiValueMap<String, String> body) {
        String length = body.getFirst("length");
        String prompt = body.getFirst("prompt");

        List<ChatMessage> messages = getChatMessages(length, prompt);
        OpenAiService service = new OpenAiService(System.getenv("OPENAI_API_KEY"));

        try {
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model(MODEL)
                    .messages(messages)
                    .temperature(TEMPERATURE)
                    .maxTokens(MAX_TOKENS)
                    .build();

            return service.createChatCompletion(chatCompletionRequest)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();
        } catch (Exception e) {
            return "An error occurred while processing your request: " + e.getMessage();
        }
    }

    @NotNull
    private static List<ChatMessage> getChatMessages(String length, String prompt) {
        String message = String.format("You are an assistant that only responds in JSON. Create a list of %s unique songs, found in the Spotify library, based off the following statement: \"%s\". Include \"id\", \"title\", \"artist\", \"album\", and \"duration\" in your response. An example response is: [{\"id\": 1,\"title\": \"Hey Jude\", \"artist\": \"The Beatles\",\"album\": \"The Beatles (White Album)\",\"duration\": \"4:56\"}].", length, prompt);

        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), message);
        messages.add(userMessage);

        return messages;
    }
}
