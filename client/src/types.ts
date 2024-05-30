export type Track = {
  title: string;
  imageUrl: string;
  artist: string;
  duration: string;
  previewUrl: string;
  externalUrl: string;
  album: string;
  spotifyId: string;
};

export type Playlist = {
  name: string;
  description: string;
  link: string;
  tracks: Track[];
};
