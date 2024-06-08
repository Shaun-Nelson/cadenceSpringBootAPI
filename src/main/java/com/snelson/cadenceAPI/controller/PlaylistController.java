package com.snelson.cadenceAPI.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.snelson.cadenceAPI.dto.PlaylistRequest;
import com.snelson.cadenceAPI.dto.SpotifyPlaylistRequestSong;
import com.snelson.cadenceAPI.model.Playlist;
import com.snelson.cadenceAPI.model.Song;
import com.snelson.cadenceAPI.model.User;
import com.snelson.cadenceAPI.repository.UserRepository;
import com.snelson.cadenceAPI.service.PlaylistService;
import com.snelson.cadenceAPI.utils.CustomGsonExclusionStrategy;
import com.snelson.cadenceAPI.utils.SecureRandomTypeAdapter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@Validated
public class PlaylistController {

    private static final Gson gson = new GsonBuilder()
            .setExclusionStrategies(new CustomGsonExclusionStrategy())
            .registerTypeAdapter(SecureRandom.class, new SecureRandomTypeAdapter())
            .create();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaylistService playlistService;

    @GetMapping
    public ResponseEntity<String> getAllPlaylists(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName());
            List<Playlist> playlists = playlistService.getAllPlaylists(user);
            if (playlists.isEmpty()) {
                return new ResponseEntity<>(gson.toJson(playlists), HttpStatus.NO_CONTENT);
            }
            return ResponseEntity.ok(gson.toJson(playlists));
        } catch (Exception e) {
            System.out.println("Error getting playlists: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Playlist> getPlaylistById(@PathVariable("id") String id) {
        try {
            Playlist playlist = playlistService.getPlaylistById(id);
            if (playlist == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            System.out.println("Error getting playlist: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<String> createPlaylist(@Valid @RequestBody String playlistJson, Authentication authentication) {
        try {
            User currentUser = userRepository.findByUsername(authentication.getName());
            PlaylistRequest requestPlaylist = playlistService.convertJsonToPlaylistRequest(playlistJson);
            Playlist newPlaylist = Playlist.builder()
                    .name(requestPlaylist.getName())
                    .description(requestPlaylist.getDescription())
                    .songs(List.of(requestPlaylist.getSongs()))
                    .build();
            playlistService.createPlaylist(newPlaylist, currentUser);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Error creating playlist: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Playlist> updatePlaylist(@PathVariable("id") String id, @Valid @RequestBody Playlist playlist) {
        try {
            Playlist updatedPlaylist = playlistService.updatePlaylist(id, playlist);
            if (updatedPlaylist == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(updatedPlaylist);
        } catch (Exception e) {
            System.out.println("Error updating playlist: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePlaylist(@PathVariable("id") String id) {
        try {
            playlistService.deletePlaylist(id);
            return new ResponseEntity<>(gson.toJson(id), HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error deleting playlist: " + e.getMessage());
            return new ResponseEntity<>(gson.toJson(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
