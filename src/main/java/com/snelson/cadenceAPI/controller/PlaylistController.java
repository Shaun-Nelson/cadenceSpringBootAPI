package com.snelson.cadenceAPI.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.snelson.cadenceAPI.model.Playlist;
import com.snelson.cadenceAPI.repository.PlaylistRepository;
import com.snelson.cadenceAPI.repository.UserRepository;
import com.snelson.cadenceAPI.service.PlaylistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@Validated
public class PlaylistController {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaylistService playlistService;

    @GetMapping("/user/{username}")
    public ResponseEntity<List<Playlist>> getPlaylists(@PathVariable("username") String username){
        try {
            return ResponseEntity.ok(playlistService.getPlaylists(username));
        } catch (Exception e) {
            System.out.println("Error getting playlists: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getPlaylistById(@PathVariable("id") String id) {
        try {
            return ResponseEntity.ok(playlistService.getPlaylistById(id).toString());
        } catch (Exception e) {
            System.out.println("Error getting playlist: " + e.getMessage());
            return ResponseEntity.badRequest().body("Playlist retrieval failed");
        }
    }

    @PostMapping
    public ResponseEntity<String> createPlaylist(@Valid @RequestBody String playlist) {
        try {
            Playlist newPlaylist = playlistService.convertJsonToPlaylist(playlist);
            if (newPlaylist == null) {
                return ResponseEntity.badRequest().body("Playlist creation failed");
            }
            playlistService.createPlaylist(newPlaylist);
            return ResponseEntity.ok(newPlaylist.toString());
        } catch (Exception e) {
            System.out.println("Error creating playlist: " + e.getMessage());
            return ResponseEntity.badRequest().body("Playlist creation failed");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updatePlaylist(@PathVariable("id") String id, @Valid @RequestBody Playlist playlist) {
        try {
            Playlist updatedPlaylist = playlistService.updatePlaylist(id, playlist);
            if (updatedPlaylist == null) {
                return ResponseEntity.badRequest().body("Playlist update failed");
            } else {
                return ResponseEntity.ok(updatedPlaylist.toString());
            }
        } catch (Exception e) {
            System.out.println("Error updating playlist: " + e.getMessage());
            return ResponseEntity.badRequest().body("Playlist update failed");
        }
    }

    @DeleteMapping("/{id}")
    public void deletePlaylist(@PathVariable("id") String id) {
        try {
            playlistService.deletePlaylist(id);
        } catch (Exception e) {
            System.out.println("Error deleting playlist: " + e.getMessage());
        }
    }

}
