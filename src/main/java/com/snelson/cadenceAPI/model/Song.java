package com.snelson.cadenceAPI.model;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Song {

    @Id
    private String id;

    @CreatedDate
    private String createdDate;

    @LastModifiedDate
    private String lastModifiedDate;

    @Version
    private Long version;

    @NotNull(message = "Title is required")
    private String title;

    @NotNull(message = "Artist is required")
    private String artist;

    @NotNull(message = "Duration is required")
    private String duration;

    private String previewUrl;
    private String imageUrl;
    private String album;
}
