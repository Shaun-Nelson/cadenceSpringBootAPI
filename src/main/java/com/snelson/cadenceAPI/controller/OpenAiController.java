package com.snelson.cadenceAPI.controller;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/openai")
public class OpenAiController {

    @PostMapping(consumes = "application/x-www-form-urlencoded", produces = "application/json")
    public String getOpenAiResponse(@RequestBody MultiValueMap<String, String> body) {
        String MODEL = "gpt-4o";
        double TEMPERATURE = 0;
        int MAX_TOKENS = 3500;
        String LENGTH = body.getFirst("length");
        String PROMPT = body.getFirst("prompt");
        List<ChatMessage> messages = getChatMessages(LENGTH, PROMPT);

        OpenAiService service = new OpenAiService(System.getenv("OPENAI_API_KEY"));
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(MODEL)
                .messages(messages)
                .temperature(TEMPERATURE)
                .maxTokens(MAX_TOKENS)
                .build();

        return service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage().getContent();
    }

    private static @NotNull List<ChatMessage> getChatMessages(String LENGTH, String PROMPT) {
        String MESSAGE = String.format("You are an assistant that only responds in JSON. Create a list of %s unique songs, found in the Spotify library, based off the following statement: \"%s\". Include \"id\", \"title\", \"artist\", \"album\", and \"duration\" in your response. An example response is: \" [{\"id\": 1,\"title\": \"Hey Jude\", \"artist\": \"The Beatles\",\"album\": \"The Beatles (White Album)\",\"duration\": \"4:56\"}]\".",
                LENGTH, PROMPT);

        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), MESSAGE);
        messages.add(userMessage);
        return messages;
    }
}
