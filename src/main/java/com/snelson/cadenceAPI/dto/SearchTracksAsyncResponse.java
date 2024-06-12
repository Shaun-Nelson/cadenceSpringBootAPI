package com.snelson.cadenceAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchTracksAsyncResponse {
    private Tracks tracks;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Tracks {
        private String href;
        private int limit;
        private String next;
        private int offset;
        private String previous;
        private int total;
        private List<Item> items;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        public static class Item {
            private Album album;
            private List<Artist> artists;
            private int disc_number;
            private int duration_ms;
            private boolean explicit;
            private ExternalIds external_ids;
            private ExternalUrls external_urls;
            private String href;
            private String id;
            private boolean is_playable;
            private String name;
            private int popularity;
            private String preview_url;
            private int track_number;
            private String type;
            private String uri;
            private boolean is_local;

            public int getDurationMs() {
                return duration_ms;
            }

            public String getPreviewUrl() {
                return preview_url;
            }

            public ExternalUrls getExternalUrls() {
                return external_urls;
            }

            @Data
            @AllArgsConstructor
            @NoArgsConstructor
            @Builder
            public static class Album {
                private String album_type;
                private int total_tracks;
                private ExternalUrls external_urls;
                private String href;
                private String id;
                private List<Image> images;
                private String name;
                private String release_date;
                private String release_date_precision;
                private String type;
                private String uri;
                private List<Artist> artists;
                private boolean is_playable;

                @Data
                @AllArgsConstructor
                @NoArgsConstructor
                @Builder
                public static class ExternalUrls {
                    private String spotify;
                }

                @Data
                @AllArgsConstructor
                @NoArgsConstructor
                @Builder
                public static class Image {
                    private String url;
                    private int height;
                    private int width;
                }

                @Data
                @AllArgsConstructor
                @NoArgsConstructor
                @Builder
                public static class Artist {
                    private ExternalUrls external_urls;
                    private String href;
                    private String id;
                    private String name;
                    private String type;
                    private String uri;

                    @Data
                    @AllArgsConstructor
                    @NoArgsConstructor
                    @Builder
                    public static class ExternalUrls {
                        private String spotify;
                    }
                }
            }

            @Data
            @AllArgsConstructor
            @NoArgsConstructor
            @Builder
            public static class Artist {
                private ExternalUrls external_urls;
                private String href;
                private String id;
                private String name;
                private String type;
                private String uri;

                @Data
                @AllArgsConstructor
                @NoArgsConstructor
                @Builder
                public static class ExternalUrls {
                    private String spotify;
                }
            }

            @Data
            @AllArgsConstructor
            @NoArgsConstructor
            @Builder
            public static class ExternalIds {
                private String isrc;
            }

            @Data
            @AllArgsConstructor
            @NoArgsConstructor
            @Builder
            public static class ExternalUrls {
                private String spotify;
            }
        }
    }
}

