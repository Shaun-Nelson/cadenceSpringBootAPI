package com.snelson.cadenceAPI.model;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document
@Getter
@Setter
public class Playlist {

        @Id
        private String id;

        private String name;
        private String description;
        private List<Song> songs;
        private String link;

        public Playlist(String id, String name, String description, List<Song> songs, String link) {
            super();
            this.id = id;
            this.name = name;
            this.description = description;
            this.songs = songs;
            this.link = link;
        }

        public int getSongCount() {
            return songs.size();
        }
}
