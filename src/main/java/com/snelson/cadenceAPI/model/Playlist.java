package com.snelson.cadenceAPI.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Playlist {

        @MongoId
        private ObjectId id;

        @CreatedDate
        private String createdDate;

        @LastModifiedDate
        private String lastModifiedDate;

        @Version
        private Long version;

        @NotNull(message = "Playlist name is required")
        private String name;

        @NotNull(message = "List of songs is required")
        private List<Song> songs;

        @DBRef
        private User user;

        private String description;
        private String link;


        public int getSongCount() {
            return songs.size();
        }
}
