package com.snelson.cadenceAPI.service;

import com.google.gson.Gson;
import com.snelson.cadenceAPI.model.Playlist;
import com.snelson.cadenceAPI.model.User;
import com.snelson.cadenceAPI.repository.PlaylistRepository;
import com.snelson.cadenceAPI.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Playlist> getAllPlaylists(HttpSession session) {
        User user = (User) session.getAttribute("user");

        return playlistRepository.findByUser(user);
    }

    public Playlist getPlaylistById(String id) {
        return playlistRepository.findById(id).orElse(null);
    }

    public void createPlaylist(Playlist playlist, HttpSession session) {
        User user = (User) session.getAttribute("user");
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

    public Playlist convertJsonToPlaylist(String json) {
        try {
            Gson gson = new Gson();

            return gson.fromJson(json, Playlist.class);
        } catch (Exception e) {
            System.out.println("Error converting JSON to Playlist: " + e.getMessage());
            return null;
        }
    }

    public Playlist getPlaylistByName(String name) {
        return playlistRepository.findByName(name);
    }
}
