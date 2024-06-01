package com.snelson.cadenceAPI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
    @PreUpdate
    public void save() {
        this.password = BCrypt.hashpw(this.password, BCrypt.gensalt(16));
    }

    public boolean isCorrectPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }

    public boolean exists() {
        return this.id != null;
    }
}
