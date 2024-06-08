package com.snelson.cadenceAPI.service;

import com.snelson.cadenceAPI.model.Song;
import com.snelson.cadenceAPI.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongService {

    @Autowired
    private SongRepository songRepository;

    public List<Song> getSongs() {
        try {
            return songRepository.findAll();
        } catch (Exception e) {
            System.out.println("Error getting songs: " + e.getMessage());
            return null;
        }
    }

    public Song getSongById(String id) {
        try {
            return songRepository.findById(id).orElse(null);
        } catch (Exception e) {
            System.out.println("Error getting song: " + e.getMessage());
            return null;
        }
    }

    public Song createSong(Song song) {
        try {
            return songRepository.save(song);
        } catch (Exception e) {
            System.out.println("Error creating song: " + e.getMessage());
            return null;
        }
    }

    public Song updateSong(String id, Song song) {
        return song;
    }

    public boolean deleteSong(String id) {
        try {
            songRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            System.out.println("Error deleting song: " + e.getMessage());
            return false;
        }
    }
}
