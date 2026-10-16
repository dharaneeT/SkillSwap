// components/studyroom/NotesPanel.jsx  (NEW FILE)
// Autosaving shared notes. This is "take turns" editing with conflict detection - NOT live Google-Docs-style editing.
import { useCallback, useEffect, useRef, useState } from "react";
import { fetchNotes, saveNotes } from "../../api/studyRoomApi";
import { getErrorMessage } from "../../api/axios";
import { formatDateTime } from "../../utils/studyRoom";

const AUTOSAVE_MS = 1500;
const MAX = 20000;

export default function NotesPanel({
  roomId,
  notesEvent,
  openCount,
  myUserId,
}) {
  const [text, setText] = useState("");
  const [meta, setMeta] = useState(null); // { version, updatedByName, updatedAt }
  const [state, setState] = useState("loading"); // loading | saved | dirty | saving | error | conflict
  const [message, setMessage] = useState("");
  const [newer, setNewer] = useState(null); // someone else saved while I have unsaved changes

  const versionRef = useRef(0);
  const dirtyRef = useRef(false);
  const textRef = useRef("");
  const savingRef = useRef(false);

  const applyServer = useCallback((n) => {
    versionRef.current = n.version;
    textRef.current = n.content;
    dirtyRef.current = false;
    setText(n.content);
    setMeta({
      version: n.version,
      updatedByName: n.updatedByName,
      updatedAt: n.updatedAt,
    });
    setState("saved");
    setNewer(null);
    setMessage("");
  }, []);

  const load = useCallback(() => {
    return fetchNotes(roomId)
      .then((res) => applyServer(res.data.data))
      .catch((e) => {
        setState("error");
        setMessage(getErrorMessage(e));
      });
  }, [roomId, applyServer]);

  // initial load + after every reconnect (we may have missed a change)
  useEffect(() => {
    if (!dirtyRef.current) load();
  }, [load, openCount]);

  // another person saved
  useEffect(() => {
    if (!notesEvent || notesEvent.version <= versionRef.current) return;
    if (notesEvent.updatedById === myUserId) return;
    if (dirtyRef.current) setNewer(notesEvent);
    else load();
  }, [notesEvent, myUserId, load]);

  const save = useCallback(
    async (force = false) => {
      if (savingRef.current) return;
      savingRef.current = true;
      setState("saving");
      const sending = textRef.current;
      try {
        const res = await saveNotes(roomId, {
          content: sending,
          baseVersion: versionRef.current,
        });
        const n = res.data.data;
        versionRef.current = n.version;
        setMeta({
          version: n.version,
          updatedByName: n.updatedByName,
          updatedAt: n.updatedAt,
        });
        if (textRef.current === sending) {
          dirtyRef.current = false;
          setState("saved");
          setNewer(null);
        } else setState("dirty"); // typed more while saving: the next autosave handles it
      } catch (e) {
        if (e?.response?.status === 409 && !force) {
          setState("conflict");
          setMessage(getErrorMessage(e));
        } else {
          setState("error");
          setMessage(getErrorMessage(e));
        }
      } finally {
        savingRef.current = false;
      }
    },
    [roomId],
  );

  // debounce: save 1.5s after the last keystroke
  useEffect(() => {
    if (state !== "dirty") return;
    const t = setTimeout(() => save(), AUTOSAVE_MS);
    return () => clearTimeout(t);
  }, [text, state, save]);

  const onChange = (e) => {
    const v = e.target.value.slice(0, MAX);
    textRef.current = v;
    dirtyRef.current = true;
    setText(v);
    if (state !== "conflict") setState("dirty");
  };

  const keepMine = async () => {
    // take the latest version number but keep my text, then save it on top
    try {
      const res = await fetchNotes(roomId);
      versionRef.current = res.data.data.version;
      await save(true);
    } catch (e) {
      setState("error");
      setMessage(getErrorMessage(e));
    }
  };

  const label = {
    loading: "Loading...",
    saved: "All changes saved",
    dirty: "Unsaved changes...",
    saving: "Saving...",
    error: "Could not save",
    conflict: "Conflict",
  }[state];

  return (
    <section aria-label="Shared notes" className="card card-white space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 className="section-title">Shared notes</h2>
        <span
          className={`badge ${state === "saved" ? "badge-success" : state === "error" || state === "conflict" ? "badge-danger" : ""}`}
          role="status"
        >
          {label}
        </span>
      </div>

      {newer && (
        <div role="alert" className="alert alert-warning flex-wrap">
          <span className="flex-1">
            {newer.updatedByName ?? "Someone"} saved a newer version while you
            were typing.
          </span>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={load}
          >
            Load theirs (discard mine)
          </button>
        </div>
      )}

      {state === "conflict" && (
        <div role="alert" className="alert alert-warning flex-wrap">
          <span className="flex-1">{message}</span>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={load}
          >
            Load latest (discard mine)
          </button>
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={keepMine}
          >
            Overwrite with mine
          </button>
        </div>
      )}

      {state === "error" && (
        <div role="alert" className="alert alert-error flex-wrap">
          <span className="flex-1">{message}</span>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={() => save()}
          >
            Retry
          </button>
        </div>
      )}

      <label>
        <span className="sr-only">Notes</span>
        <textarea
          className="textarea min-h-72 font-mono text-sm"
          value={text}
          onChange={onChange}
          disabled={state === "loading"}
          placeholder="Write notes everyone in the room can see..."
        />
      </label>

      <p className="meta">
        {meta?.updatedAt
          ? `Last updated ${formatDateTime(meta.updatedAt)}${meta.updatedByName ? ` by ${meta.updatedByName}` : ""}`
          : "Nothing saved yet"}
        {" · "}
        {text.length} / {MAX}
      </p>
      <p className="meta">
        Notes save automatically. If two people edit at once, the second save is
        stopped and you choose what to keep.
      </p>
    </section>
  );
}
