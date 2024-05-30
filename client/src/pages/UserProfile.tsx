import { useSelector } from "react-redux";
import { useLoginSpotifyMutation } from "../slices/thirdPartyApiSlice";
import { RootState } from "../store";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faSpotify } from "@fortawesome/free-brands-svg-icons";
import { toast } from "react-toastify";

const UserProfile = () => {
  const { userInfo } = useSelector((state: RootState) => state.auth);

  const [loginSpotify] = useLoginSpotifyMutation();

  const handleSpotfiyConnect = async () => {
    try {
      const res = await loginSpotify({}).unwrap();
      console.log(res);

      window.location.href = res.url;
    } catch (error) {
      console.error(error);
      toast.error("Failed to connect to Spotify");
    }
  };

  return (
    <div className='flex-container-column'>
      <h3>{userInfo.username}'s Profile</h3>
      <hr />
      <span>Log in to Spotify to save playlists to your Spotify account.</span>
      <FontAwesomeIcon
        className='icon-save-playlist-spotify'
        style={{ paddingLeft: "15px" }}
        icon={faSpotify}
        onClick={handleSpotfiyConnect}
        title='Save playlist to Spotify account'
      />
    </div>
  );
};

export default UserProfile;
