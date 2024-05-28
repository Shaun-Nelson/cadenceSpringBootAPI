package com.snelson.cadenceAPI.model;

import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.List;

import static org.springframework.data.mongodb.core.schema.JsonSchemaProperty.string;

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

    @NotNull(message = "Username is required")
    private String username;

    @NotNull(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters long")
    private String password;

    private String email;

    @DBRef
    private List<Playlist> playlists;

    @PrePersist
    public void save() {
        int saltRounds = 10;
        this.password = BCrypt.hashpw(this.password, BCrypt.gensalt(saltRounds));
    }

    public boolean isCorrectPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }

    public boolean exists() {
        return this.id != null;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", lastModifiedDate='" + lastModifiedDate + '\'' +
                ", version=" + version +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", playlists=" + playlists +
                '}';
    }
}
