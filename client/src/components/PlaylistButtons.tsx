import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFloppyDisk } from "@fortawesome/free-solid-svg-icons";
import { faSpotify } from "@fortawesome/free-brands-svg-icons";
import { useState } from "react";
import { useSelector } from "react-redux";
import {
  useCreatePlaylistMutation,
  useSaveSpotifyPlaylistMutation,
} from "../slices/playlistApiSlice";
import { RootState } from "../store";
import { toast } from "react-toastify";

const PlaylistButtons = () => {
  const [playlistName, setPlaylistName] = useState<string>("");
  const [playlistDescription, setPlaylistDescription] = useState<string>("");
  const [error, setError] = useState<string>("");
  const [success, setSuccess] = useState<string>("");

  const { results } = useSelector((state: RootState) => state.results);
  const [createPlaylist, { isError }] = useCreatePlaylistMutation();
  const [saveSpotifyPlaylist] = useSaveSpotifyPlaylistMutation();

  const handleLocalSave = async () => {
    try {
      await createPlaylist({
        name: playlistName,
        description: playlistDescription,
        songs: JSON.stringify(results),
      });

      if (isError) {
        setError("Please log in to save playlist.");
        setSuccess("");
      } else {
        setSuccess("Playlist saved!");
        setError("");
      }
    } catch (error) {
      console.error(error);
      toast.error("Cannot save playlist. Please log in.");
    }
  };

  const handleSpotfiySave = async () => {
    try {
      await saveSpotifyPlaylist({
        name: playlistName,
        description: playlistDescription,
        songs: JSON.stringify(results),
      }).unwrap();
      toast.success("Playlist saved to Spotify!");
    } catch (error) {
      console.error(error);
      toast.error(
        "Error saving playlist to Spotify. Please connect via User Profile."
      );
    }
  };

  return (
    <>
      {results.length > 0 && (
        <div className='flex-container-column'>
          {error && <span className='error-message'>{error}</span>}
          {success && <span className='success-message'>{success}</span>}
          <div id='playlist-buttons'>
            <form className='playlist-form' style={{ paddingRight: "15px" }}>
              <input
                type='text'
                placeholder='Playlist Name'
                required
                name={playlistName}
                id='playlistName'
                style={{ paddingRight: "5px" }}
                onChange={(e) => setPlaylistName(e.target.value)}
              />
              <input
                type='text'
                placeholder='Playlist Description'
                name={playlistDescription}
                id='playlistDescription'
                onChange={(e) => setPlaylistDescription(e.target.value)}
              />
            </form>
            <FontAwesomeIcon
              className={
                error
                  ? "icon-save-playlist-local-error"
                  : "icon-save-playlist-local"
              }
              icon={faFloppyDisk}
              style={{ paddingRight: "15px" }}
              onClick={handleLocalSave}
              title='Save playlist to local user account'
            />
            <FontAwesomeIcon
              className={
                error
                  ? "icon-save-playlist-spotify-error"
                  : "icon-save-playlist-spotify"
              }
              icon={faSpotify}
              onClick={handleSpotfiySave}
              title='Save playlist to Spotify account'
            />
          </div>
        </div>
      )}
    </>
  );
};

export default PlaylistButtons;
