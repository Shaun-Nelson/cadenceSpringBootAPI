package com.snelson.cadenceAPI.repository;

import com.snelson.cadenceAPI.model.Song;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SongRepository extends MongoRepository<Song, String> {

    public Song findByTitle(String title);
    public Song findByTitleAndArtist(String title, String artist);
    public List<Song> findAllByArtist(String artist);
}
