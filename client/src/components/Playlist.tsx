import { faTrash } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

// Types
import type { Playlist, Track } from "../types";

interface PlaylistProps {
  playlist: Playlist;
  handlePlaylistDelete?: (name: string) => void;
}

const Playlist = ({ playlist, handlePlaylistDelete }: PlaylistProps) => {
  return (
    <>
      {handlePlaylistDelete && (
        <div className='playlist-metainfo'>
          <h2>{playlist.name}</h2>
          <p>{playlist.description}</p>
          <a href={playlist.link} target='_blank' rel='noreferrer'>
            Spotify Playlist
          </a>
          <FontAwesomeIcon
            onClick={() => handlePlaylistDelete(playlist.name)}
            icon={faTrash}
          />
        </div>
      )}

      <table id='table-playlist-results'>
        <tbody>
          <tr>
            <th>Album</th>
            <th>Title</th>
            <th>Artists</th>
            <th>Duration</th>
            <th>Preview</th>
          </tr>
          {playlist.tracks.map((track: Track, index: number) => {
            return (
              <tr key={index}>
                <td>
                  <img src={track.imageUrl} alt='album cover' height={"50px"} />
                </td>
                <td>
                  <a href={track.externalUrl} target='_blank' rel='noreferrer'>
                    {track.title}
                  </a>
                </td>
                <td>
                  <p>{track.artist}</p>
                </td>
                <td>
                  <p>{track.duration}</p>
                </td>
                <td>
                  <audio controls>
                    <source src={track.previewUrl} type='audio/mpeg' />
                  </audio>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </>
  );
};

export default Playlist;
