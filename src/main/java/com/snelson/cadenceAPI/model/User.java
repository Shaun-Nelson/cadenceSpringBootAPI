package com.snelson.cadenceAPI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements Serializable {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @MongoId
    private ObjectId id;

    @CreatedDate
    private String createdDate;

    @LastModifiedDate
    private String lastModifiedDate;

    @Version
    private Long version;

    @NotBlank
    @NotNull(message = "Username is required")
    private String username;

    @NotBlank
    @NotNull(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters long")
    private String password;

    @Email
    private String email;

    @DBRef
    private Set<Role> roles = new HashSet<>();

    private boolean enabled;

    @DBRef
    private List<Playlist> playlists;

    public User(String username, String password, Set<GrantedAuthority> grantedAuthorities) {
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>();
        grantedAuthorities.forEach(authority -> roles.add(new Role(authority.getAuthority())));
    }

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
