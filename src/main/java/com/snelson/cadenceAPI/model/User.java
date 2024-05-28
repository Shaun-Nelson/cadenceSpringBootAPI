package com.snelson.cadenceAPI.model;

import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.List;

@Document
@Getter
@Setter
public class User {

    @Id
    private String id;

    private String username;
    private String password;
    private String email;
    private List<Playlist> playlists;

    public User(String id, String username, String password, String email, List<Playlist> playlists) {
        super();
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.playlists = playlists;
    }

    @PrePersist
    public void save() {
        int saltRounds = 10;
        this.password = BCrypt.hashpw(this.password, BCrypt.gensalt(saltRounds));
    }

    public boolean isCorrectPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }
}
