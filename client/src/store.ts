import { configureStore, combineReducers } from "@reduxjs/toolkit";
import authReducer from "./slices/authSlice";
import resultsReducer from "./slices/resultsSlice";
import { apiSlice } from "./slices/apiSlice";

const rootReducer = combineReducers({});

export type RootState = ReturnType<typeof rootReducer>;

const store = configureStore({
  reducer: {
    auth: authReducer,
    [apiSlice.reducerPath]: apiSlice.reducer,
    results: resultsReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(apiSlice.middleware),
  devTools: import.meta.env.NODE_ENV !== "production",
});

export default store;
