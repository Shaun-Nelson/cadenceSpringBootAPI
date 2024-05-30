import { apiSlice } from "./apiSlice";

const API_URL = "/api/playlists";

export const playlistsApiSlice = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    getPlaylists: builder.mutation({
      query: () => ({
        url: `${API_URL}`,
        method: "GET",
        withCredentials: true,
      }),
    }),
    createPlaylist: builder.mutation({
      query: (data) => ({
        url: `${API_URL}`,
        method: "POST",
        body: data,
        withCredentials: true,
      }),
    }),
    deletePlaylist: builder.mutation({
      query: (data) => ({
        url: `${API_URL}`,
        method: "DELETE",
        body: data,
        withCredentials: true,
      }),
    }),
    saveSpotifyPlaylist: builder.mutation({
      query: (data) => ({
        url: `${API_URL}/spotify`,
        method: "POST",
        body: data,
        withCredentials: true,
      }),
    }),
  }),
});

export const {
  useGetPlaylistsMutation,
  useCreatePlaylistMutation,
  useDeletePlaylistMutation,
  useSaveSpotifyPlaylistMutation,
} = playlistsApiSlice;
