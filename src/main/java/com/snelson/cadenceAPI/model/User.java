package com.snelson.cadenceAPI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serializable;
import java.util.List;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements Serializable {

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    private String role;

    private String principalName;

    private boolean enabled;

    @DBRef
    private List<Playlist> playlists;

    @PrePersist
    @PreUpdate
    public void savePassword() {
        if (!isCorrectPassword(this.password))  {
            this.password = passwordEncoder.encode(this.password);
        }
    }

    public boolean isCorrectPassword(String password) {
        return passwordEncoder.matches(password, this.password);
    }
}
