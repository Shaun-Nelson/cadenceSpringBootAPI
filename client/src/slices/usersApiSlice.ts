import { apiSlice } from "./apiSlice";

const USERS_URL = "/api";

export const usersApiSlice = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    login: builder.mutation({
      query: (data) => ({
        url: `${USERS_URL}/login`,
        method: "POST",
        body: data,
        withCredentials: true,
      }),
    }),
    signup: builder.mutation({
      query: (data) => ({
        url: `${USERS_URL}/signup`,
        method: "POST",
        body: data,
      }),
    }),
    isLoggedIn: builder.mutation({
      query: () => ({
        url: `${USERS_URL}/login`,
      }),
    }),
    logout: builder.mutation({
      query: () => ({
        url: `${USERS_URL}/logout`,
        withCredentials: true,
      }),
    }),
    loginSpotify: builder.mutation({
      query: () => ({
        url: `${USERS_URL}/login/spotify`,
        withCredentials: true,
      }),
    }),
  }),
});

export const {
  useLoginMutation,
  useIsLoggedInMutation,
  useLogoutMutation,
  useLoginSpotifyMutation,
  useSignupMutation,
} = usersApiSlice;
