package com.snelson.cadenceAPI.service;

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
        try {
            User user = userRepository.findByUsername(username);
            return playlistRepository.findAllByUser(user);
        } catch (Exception e) {
            System.out.println("Error getting playlists: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Playlist getPlaylistById(String id) {
        try {
            return playlistRepository.findById(id).orElse(null);
        } catch (Exception e) {
            System.out.println("Error getting playlist: " + e.getMessage());
            return null;
        }
    }

    public Playlist createPlaylist(String username, Playlist playlist) {
        try {
            User user = userRepository.findByUsername(username);
            playlist.setUser(user);
            return playlistRepository.save(playlist);
        } catch (Exception e) {
            System.out.println("Error creating playlist: " + e.getMessage());
            return null;
        }
    }

    public Playlist updatePlaylist(String id, Playlist playlist) {
        try {
            Playlist existingPlaylist = playlistRepository.findById(id).orElse(null);
            if (existingPlaylist == null) {
                return null;
            }
            existingPlaylist.setName(playlist.getName());
            existingPlaylist.setSongs(playlist.getSongs());
            return playlistRepository.save(existingPlaylist);
        } catch (Exception e) {
            System.out.println("Error updating playlist: " + e.getMessage());
            return null;
        }
    }

    public void deletePlaylist(String id) {
        try {
            playlistRepository.deleteById(id);
        } catch (Exception e) {
            System.out.println("Error deleting playlist: " + e.getMessage());
        }
    }
}
