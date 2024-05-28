package com.snelson.cadenceAPI.controller;

import com.snelson.cadenceAPI.model.Song;
import com.snelson.cadenceAPI.repository.SongRepository;
import com.snelson.cadenceAPI.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
@Validated
public class SongController {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private SongService songService;

    @GetMapping
    public List<Song> getSongs() {
        return songService.getSongs();
    }

    @GetMapping("/{id}")
    public Song getSongById(@PathVariable("id") String id) {
        return songService.getSongById(id);
    }

    @PostMapping
    public Song createSong(@RequestBody Song song) {
        return songService.createSong(song);
    }

    @PutMapping("/{id}")
    public Song updateSong(@PathVariable("id") String id, @RequestBody Song song) {
        return songService.updateSong(id, song);
    }

    @DeleteMapping("/{id}")
    public boolean deleteSong(@PathVariable("id") String id) {
        return songService.deleteSong(id);
    }
}
