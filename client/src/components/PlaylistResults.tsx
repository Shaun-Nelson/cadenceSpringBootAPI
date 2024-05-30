import { useSelector } from "react-redux";
import { RootState } from "../store";
// Components
import PlaylistComponent from "./Playlist";
// Types
import type { Playlist } from "../types";

const PlaylistResults = () => {
  const { results } = useSelector((state: RootState) => state.results);
  const playlist: Playlist = {
    name: "",
    description: "",
    link: "",
    tracks: results,
  };

  return (
    <>{results?.length > 0 && <PlaylistComponent playlist={playlist} />}</>
  );
};

export default PlaylistResults;
