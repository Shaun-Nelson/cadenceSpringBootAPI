package com.snelson.cadenceAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpotifyPlaylistRequestSong {

    private String album;
    private String artist;
    private String duration;
    private String externalUrl;
    private String imageUrl;
    private String spotifyId;
    private String title;
}
