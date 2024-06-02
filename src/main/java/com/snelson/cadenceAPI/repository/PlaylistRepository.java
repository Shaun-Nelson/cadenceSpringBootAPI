package com.snelson.cadenceAPI.repository;

import com.snelson.cadenceAPI.model.Playlist;
import com.snelson.cadenceAPI.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PlaylistRepository extends MongoRepository<Playlist, String> {

        List<Playlist> findAllByUser(User user);
}
