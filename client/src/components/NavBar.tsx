import { useState, useEffect } from "react";
import { useLogoutMutation } from "../slices/usersApiSlice";
import { logout } from "../slices/authSlice";
import { useSelector, useDispatch } from "react-redux";
import { RootState } from "../store";
import { toast } from "react-toastify";

// Components
import NavItem from "./NavItem";

const NavBar = () => {
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(false);

  const [logoutUser] = useLogoutMutation();
  const dispatch = useDispatch();

  const { userInfo } = useSelector((state: RootState) => state.auth);

  const handleLogout = async () => {
    try {
      dispatch(logout());
      await logoutUser({}).unwrap();
      setIsLoggedIn(false);
    } catch (error) {
      console.error(error);
      toast.error("Failed to logout");
    }
  };

  useEffect(() => {
    if (userInfo) {
      setIsLoggedIn(true);
    } else {
      setIsLoggedIn(false);
    }
  }, [userInfo]);

  return (
    <nav>
      <ul className='navbar'>
        <NavItem linkTo='/' bodyText='Home' />
        <NavItem linkTo='/signup' bodyText='Sign-Up' />
        {isLoggedIn ? (
          <>
            <NavItem linkTo='/playlists' bodyText='My Playlists' />
            <NavItem linkTo='/profile' bodyText='User Profile' />
            <NavItem
              linkTo='/logout'
              bodyText='Logout'
              onClickHandler={handleLogout}
            />
          </>
        ) : (
          <NavItem linkTo='/login' bodyText='Login' />
        )}
      </ul>
    </nav>
  );
};

export default NavBar;
