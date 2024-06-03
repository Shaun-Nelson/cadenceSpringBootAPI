package com.snelson.cadenceAPI.controller;

import com.google.gson.Gson;
import com.snelson.cadenceAPI.model.Playlist;
import com.snelson.cadenceAPI.service.PlaylistService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@Validated
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    @GetMapping
    public ResponseEntity<String> getAllPlaylists(HttpSession session) {
        try {
            List<Playlist> playlists = playlistService.getAllPlaylists(session);
            if (playlists.isEmpty()) {
                return new ResponseEntity<>(new Gson().toJson(playlists), HttpStatus.NO_CONTENT);
            }
            return ResponseEntity.ok(new Gson().toJson(playlists));
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
    public ResponseEntity<String> createPlaylist(@Valid @RequestBody String playlistJson, HttpSession session) {
        try {
            Playlist newPlaylist = playlistService.convertJsonToPlaylist(playlistJson);
            if (newPlaylist == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            playlistService.createPlaylist(newPlaylist, session);

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
    public ResponseEntity<Void> deletePlaylist(@PathVariable("id") String id) {
        try {
            playlistService.deletePlaylist(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            System.out.println("Error deleting playlist: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
