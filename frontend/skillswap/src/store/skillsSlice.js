import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { fetchSkills } from "../api/skillApi";
import { getErrorMessage } from "../api/axios";

const initialState = { items: [], status: "idle", error: "" };

// dispatch(loadSkills())     → fetch only if not already loaded
// dispatch(loadSkills(true)) → force refresh (e.g. after adding a skill)
export const loadSkills = createAsyncThunk(
  "skills/load",
  async (_force, { rejectWithValue }) => {
    try {
      const res = await fetchSkills();
      return res.data.data;
    } catch (e) {
      return rejectWithValue(getErrorMessage(e));
    }
  },
  {
    condition: (force, { getState }) => {
      const { status } = getState().skills;
      if (status === "loading") return false; // already in flight
      if (status === "succeeded" && !force) return false; // cached
    },
  },
);

const skillsSlice = createSlice({
  name: "skills",
  initialState,
  reducers: {
    skillsCleared: () => initialState,
  },
  extraReducers: (builder) => {
    builder
      .addCase(loadSkills.pending, (s) => {
        s.status = "loading";
        s.error = "";
      })
      .addCase(loadSkills.fulfilled, (s, a) => {
        s.status = "succeeded";
        s.items = a.payload;
      })
      .addCase(loadSkills.rejected, (s, a) => {
        s.status = "failed";
        s.error = a.payload ?? a.error.message;
      });
  },
});

export const { skillsCleared } = skillsSlice.actions;
export default skillsSlice.reducer;
