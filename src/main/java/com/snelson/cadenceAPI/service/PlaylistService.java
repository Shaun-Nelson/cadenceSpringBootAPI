package com.snelson.cadenceAPI.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.snelson.cadenceAPI.dto.PlaylistRequest;
import com.snelson.cadenceAPI.model.Playlist;
import com.snelson.cadenceAPI.model.User;
import com.snelson.cadenceAPI.repository.PlaylistRepository;
import com.snelson.cadenceAPI.repository.UserRepository;
import com.snelson.cadenceAPI.utils.CustomGsonExclusionStrategy;
import com.snelson.cadenceAPI.utils.SecureRandomTypeAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class PlaylistService {

    private static final Gson gson = new GsonBuilder()
            .setExclusionStrategies(new CustomGsonExclusionStrategy())
            .registerTypeAdapter(SecureRandom.class, new SecureRandomTypeAdapter())
            .create();

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Playlist> getAllPlaylists(User user) {
        return playlistRepository.findByUser(user);
    }

    public Playlist getPlaylistById(String id) {
        return playlistRepository.findById(id).orElse(null);
    }

    public void createPlaylist(Playlist playlist, User user) {
        playlist.setUser(user);
        playlistRepository.save(playlist);
    }

    public Playlist updatePlaylist(String id, Playlist playlist) {
        Playlist existingPlaylist = playlistRepository.findById(id).orElse(null);
        if (existingPlaylist == null) {
            return null;
        }
        existingPlaylist.setName(playlist.getName());
        existingPlaylist.setSongs(playlist.getSongs());
        existingPlaylist.setDescription(playlist.getDescription());
        existingPlaylist.setLink(playlist.getLink());
        return playlistRepository.save(existingPlaylist);
    }

    public void deletePlaylist(String id) {
        playlistRepository.deleteById(id);
    }

    public PlaylistRequest convertJsonToPlaylistRequest(String json) {
        try {
            return gson.fromJson(json, PlaylistRequest.class);
        } catch (Exception e) {
            System.out.println("Error converting JSON to Playlist: " + e.getMessage());
            return null;
        }
    }

    public Playlist getPlaylistByName(String name) {
        return playlistRepository.findByName(name);
    }
}
