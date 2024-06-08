package com.snelson.cadenceAPI.dto;

import com.snelson.cadenceAPI.model.Song;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaylistRequest {

    private String name;
    private String description;
    private Song[] songs;
}
