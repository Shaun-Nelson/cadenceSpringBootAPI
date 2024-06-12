package com.snelson.cadenceAPI.controller;

import com.google.gson.*;
import com.snelson.cadenceAPI.model.Song;
import com.snelson.cadenceAPI.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/api/openai")
public class OpenAiController {

    @Autowired
    private OpenAiService openAiService;

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getOpenAiResponseFromForm(@RequestBody MultiValueMap<String, String> formBody) {
        String length = formBody.getFirst("length");
        String input = formBody.getFirst("input");
        return ResponseEntity.ok(openAiService.processRequest(length, input));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getOpenAiResponseFromJson(@RequestBody String jsonBody) {
        JsonObject jsonObject = JsonParser.parseString(jsonBody).getAsJsonObject();
        String length = jsonObject.get("length").getAsString();
        String input = jsonObject.get("input").getAsString();
        return ResponseEntity.ok(openAiService.processRequest(length, input));
    }
}