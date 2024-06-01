package com.snelson.cadenceAPI.service;

import com.google.gson.Gson;
import com.snelson.cadenceAPI.model.Playlist;
import com.snelson.cadenceAPI.model.User;
import com.snelson.cadenceAPI.repository.PlaylistRepository;
import com.snelson.cadenceAPI.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Playlist> getPlaylists(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return new ArrayList<>();
        }
        return playlistRepository.findAllByUser(user);
    }

    public Playlist getPlaylistById(String id) {
        return playlistRepository.findById(id).orElse(null);
    }

    public Playlist createPlaylist(Playlist playlist) {
        return playlistRepository.save(playlist);
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

    public Playlist convertJsonToPlaylist(String json) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(json, Playlist.class);
        } catch (Exception e) {
            System.out.println("Error converting JSON to Playlist: " + e.getMessage());
            return null;
        }
    }
}
