// pages/StudyRoomsPage.jsx  (NEW FILE)  route: /study-rooms
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { createRoom, fetchRooms, joinRoom } from "../api/studyRoomApi";
import { getErrorMessage } from "../api/axios";
import { useFetch } from "../hooks/useFetch";
import { usePolling } from "../hooks/usePolling";
import Icon from "../components/Icon";
import RoomCard from "../components/studyroom/RoomCard";
import RoomFormModal from "../components/studyroom/RoomFormModal";
import { DIFFICULTIES, ROOM_FILTERS, TOPICS } from "../utils/studyRoom";

const PAGE_SIZE = 12;

export default function StudyRoomsPage() {
  const navigate = useNavigate();
  const [search, setSearch] = useState("");
  const [q, setQ] = useState("");
  const [topic, setTopic] = useState("");
  const [difficulty, setDifficulty] = useState("");
  const [filter, setFilter] = useState("ALL");
  const [page, setPage] = useState(0);
  const [creating, setCreating] = useState(false);
  const [busyId, setBusyId] = useState(null);
  const [actionError, setActionError] = useState("");
  const [codeRoomId, setCodeRoomId] = useState("");
  const [code, setCode] = useState("");

  // wait until the user stops typing before asking the server
  useEffect(() => {
    const t = setTimeout(() => {
      setQ(search.trim());
      setPage(0);
    }, 300);
    return () => clearTimeout(t);
  }, [search]);

  const rooms = useFetch(fetchRooms, {
    q: q || undefined,
    topic: topic || undefined,
    difficulty: difficulty || undefined,
    filter,
    page,
    size: PAGE_SIZE,
  });
  usePolling(rooms.refetch, 20000); // keeps "who is here" counts fresh

  const items = rooms.data?.content ?? [];
  const filtersOn = Boolean(q || topic || difficulty || filter !== "ALL");

  const open = async (room) => {
    setActionError("");
    if (room.member) return navigate(`/study-rooms/${room.id}`);
    setBusyId(room.id);
    try {
      await joinRoom(room.id);
      navigate(`/study-rooms/${room.id}`);
    } catch (e) {
      setActionError(getErrorMessage(e));
      rooms.refetch();
    } finally {
      setBusyId(null);
    }
  };

  const joinPrivate = async (e) => {
    e.preventDefault();
    setActionError("");
    try {
      await joinRoom(Number(codeRoomId), code.trim());
      navigate(`/study-rooms/${Number(codeRoomId)}`);
    } catch (err) {
      setActionError(getErrorMessage(err));
    }
  };

  const create = async (body) => {
    const res = await createRoom(body);
    navigate(`/study-rooms/${res.data.data.room.id}`);
  };

  const clearFilters = () => {
    setSearch("");
    setQ("");
    setTopic("");
    setDifficulty("");
    setFilter("ALL");
    setPage(0);
  };

  return (
    <main className="page max-w-6xl space-y-6">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <div className="flex flex-col gap-2">
          <p className="eyebrow">Learn together</p>
          <h1 className="page-title">Study rooms</h1>
          <p className="page-sub max-w-xl">
            Join people studying the same topic: chat, share a focus timer, keep
            notes together and earn credits for showing up.
          </p>
        </div>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => setCreating(true)}
        >
          <Icon name="plus" size={16} /> Create room
        </button>
      </header>

      <section aria-label="Find a room" className="card card-white space-y-4">
        <div className="relative">
          <Icon name="search" size={16} className="input-icon" />
          <input
            type="search"
            className="input input-with-icon"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            maxLength={80}
            placeholder="Search rooms by name, topic or description"
            aria-label="Search rooms"
          />
        </div>

        <div className="flex flex-wrap gap-2" role="group" aria-label="Topic">
          <button
            type="button"
            className="chip"
            aria-pressed={topic === ""}
            onClick={() => {
              setTopic("");
              setPage(0);
            }}
          >
            All topics
          </button>
          {TOPICS.map((t) => (
            <button
              key={t}
              type="button"
              className="chip"
              aria-pressed={topic === t}
              onClick={() => {
                setTopic(topic === t ? "" : t);
                setPage(0);
              }}
            >
              {t}
            </button>
          ))}
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {ROOM_FILTERS.map((f) => (
            <button
              key={f.value}
              type="button"
              className="chip"
              aria-pressed={filter === f.value}
              onClick={() => {
                setFilter(f.value);
                setPage(0);
              }}
            >
              {f.label}
            </button>
          ))}
          <select
            className="input select ml-auto"
            aria-label="Difficulty"
            value={difficulty}
            onChange={(e) => {
              setDifficulty(e.target.value);
              setPage(0);
            }}
          >
            <option value="">Any difficulty</option>
            {DIFFICULTIES.map((d) => (
              <option key={d.value} value={d.value}>
                {d.label}
              </option>
            ))}
          </select>
        </div>
      </section>

      <form
        onSubmit={joinPrivate}
        className="flex flex-wrap items-end gap-2"
        aria-label="Join a private room"
      >
        <label className="space-y-1">
          <span className="meta block">
            Private room? Enter its ID and code
          </span>
          <span className="flex flex-wrap gap-2">
            <input
              className="input w-28"
              type="number"
              min={1}
              required
              placeholder="Room ID"
              value={codeRoomId}
              onChange={(e) => setCodeRoomId(e.target.value)}
            />
            <input
              className="input w-40 uppercase"
              required
              maxLength={12}
              placeholder="Access code"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              autoComplete="off"
            />
          </span>
        </label>
        <button type="submit" className="btn btn-secondary">
          <Icon name="lock" size={15} /> Join
        </button>
      </form>

      {(actionError || rooms.error) && (
        <div role="alert" className="alert alert-error">
          <Icon name="alert-circle" size={18} className="mt-0.5" />
          <span className="flex-1">{actionError || rooms.error}</span>
          {rooms.error && !actionError && (
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={rooms.refetch}
            >
              Retry
            </button>
          )}
        </div>
      )}

      {!rooms.data && rooms.loading && (
        <p className="text-sm text-muted">Loading rooms...</p>
      )}

      {rooms.data && items.length === 0 && (
        <div className="card flex flex-col items-start gap-3">
          <p className="flex items-center gap-2 text-sm text-muted">
            <Icon name="users" size={16} />
            {filtersOn
              ? "No rooms match these filters."
              : "No study rooms yet. Be the first to host one."}
          </p>
          <div className="flex gap-2">
            {filtersOn && (
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={clearFilters}
              >
                Clear filters
              </button>
            )}
            <button
              type="button"
              className="btn btn-primary btn-sm"
              onClick={() => setCreating(true)}
            >
              Create a room
            </button>
          </div>
        </div>
      )}

      {items.length > 0 && (
        <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {items.map((r) => (
            <li key={r.id}>
              <RoomCard room={r} busy={busyId === r.id} onJoin={open} />
            </li>
          ))}
        </ul>
      )}

      {rooms.data && rooms.data.totalPages > 1 && (
        <nav
          className="flex items-center justify-center gap-3"
          aria-label="Pages"
        >
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            disabled={!rooms.data.hasPrevious}
            onClick={() => setPage((p) => p - 1)}
          >
            <Icon name="chevron-left" size={15} /> Previous
          </button>
          <span className="meta">
            Page {rooms.data.page + 1} of {rooms.data.totalPages}
          </span>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            disabled={!rooms.data.hasNext}
            onClick={() => setPage((p) => p + 1)}
          >
            Next <Icon name="chevron-right" size={15} />
          </button>
        </nav>
      )}

      {creating && (
        <RoomFormModal
          title="Create a study room"
          submitLabel="Create room"
          onSubmit={create}
          onClose={() => setCreating(false)}
        />
      )}
    </main>
  );
}
