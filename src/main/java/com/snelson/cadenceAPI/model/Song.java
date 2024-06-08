package com.snelson.cadenceAPI.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Song {

    @Id
    private ObjectId id;

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

    private String duration;
    private String previewUrl;
    private String externalUrl;
    private String imageUrl;
    private String album;
    private String spotifyId;
}
