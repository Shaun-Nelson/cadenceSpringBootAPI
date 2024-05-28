package com.snelson.cadenceAPI.model;

import jakarta.persistence.Id;
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

    private String title;
    private String artist;
    private String duration;
    private String previewUrl;
    private String imageUrl;


    @Override
    public String toString() {
        return "Song{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", duration='" + duration + '\'' +
                ", previewUrl='" + previewUrl + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
