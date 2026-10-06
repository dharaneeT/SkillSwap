import { configureStore } from "@reduxjs/toolkit";
import skillsReducer from "./skillsSlice";
import sessionsReducer from "./sessionsSlice";

export const store = configureStore({
  reducer: {
    skills: skillsReducer,
    sessions: sessionsReducer,
  },
});
