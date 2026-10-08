import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { bookSession, fetchMySessions, updateSession } from "../api/sessionApi";
import { getErrorMessage } from "../api/axios";

const toPayload = (e) => ({
  message: getErrorMessage(e),
  status: e?.response?.status ?? null,
});

const initialState = { items: [], status: "idle", error: "" };

export const loadSessions = createAsyncThunk(
  "sessions/load",
  async (_, { rejectWithValue }) => {
    try {
      const res = await fetchMySessions();
      return res.data.data;
    } catch (e) {
      return rejectWithValue(getErrorMessage(e));
    }
  },
);

// book / change status, then reload so the list matches the server
export const bookNewSession = createAsyncThunk(
  "sessions/book",
  async (payload, { dispatch, rejectWithValue }) => {
    try {
      await bookSession(payload); // { providerId, skillId, sessionTime }
      await dispatch(loadSessions());
    } catch (e) {
      return rejectWithValue(toPayload(e));
    }
  },
);

export const changeSessionStatus = createAsyncThunk(
  "sessions/changeStatus",
  async ({ id, status }, { dispatch, rejectWithValue }) => {
    try {
      await updateSession(id, status); // ACCEPTED | REJECTED | COMPLETED | CANCELLED
      await dispatch(loadSessions());
    } catch (e) {
      return rejectWithValue(toPayload(e));
    }
  },
);

const sessionsSlice = createSlice({
  name: "sessions",
  initialState,
  reducers: {
    sessionsCleared: () => initialState,
  },
  extraReducers: (builder) => {
    builder
      .addCase(loadSessions.pending, (s) => {
        if (s.status !== "succeeded") s.status = "loading"; // background refreshes stay "succeeded"
        s.error = "";
      })
      .addCase(loadSessions.fulfilled, (s, a) => {
        s.status = "succeeded";
        s.items = a.payload;
      })
      .addCase(loadSessions.rejected, (s, a) => {
        s.status = "failed";
        s.error = a.payload ?? a.error.message;
      });
  },
});

export const { sessionsCleared } = sessionsSlice.actions;
export default sessionsSlice.reducer;
