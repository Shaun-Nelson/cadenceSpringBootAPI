package com.snelson.cadenceAPI.repository;

import com.snelson.cadenceAPI.model.Playlist;
import com.snelson.cadenceAPI.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PlaylistRepository extends MongoRepository<Playlist, String> {

        List<Playlist> findByUser(User user);

        Playlist findByName(String name);
}
