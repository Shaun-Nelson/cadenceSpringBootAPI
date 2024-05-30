import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  userInfo: localStorage.getItem("userInfo")
    ? JSON.parse(localStorage.getItem("userInfo") || '"')
    : null,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setCredentials: (state, action) => {
      state.userInfo = action.payload;
      localStorage.setItem("userInfo", JSON.stringify(action.payload));
    },
    logout: (state) => {
      state.userInfo = null;
      localStorage.removeItem("userInfo");
    },
    loginSpotify: () => {
      fetch("/api/login/spotify", {
        method: "GET",
        credentials: "include",
      });
    },
  },
});

export const { setCredentials, logout, loginSpotify } = authSlice.actions;

export default authSlice.reducer;
