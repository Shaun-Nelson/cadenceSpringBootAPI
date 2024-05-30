import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  results: [],
};

const resultsSlice = createSlice({
  name: "results",
  initialState,
  reducers: {
    setResults: (state, action) => {
      state.results = action.payload;
    },
  },
});

export const { setResults } = resultsSlice.actions;

export default resultsSlice.reducer;
