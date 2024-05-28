package com.snelson.cadenceAPI.model;

import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.List;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    @CreatedDate
    private String createdDate;

    @LastModifiedDate
    private String lastModifiedDate;

    @Version
    private Long version;

    private String username;
    private String password;
    private String email;
    private List<Playlist> playlists;

    public User(String username, String password, String email, List<Playlist> playlists) {
        super();
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

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", playlists=" + playlists +
                '}';
    }
}
