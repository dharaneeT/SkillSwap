import { useEffect, useMemo, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useAuth } from "../hooks/useAuth";
import { useFetch } from "../hooks/useFetch";
import { loadSkills } from "../store/skillsSlice";
import {
  activateUser,
  addCredits,
  deactivateUser,
  deleteSkill,
  fetchUsers,
  setUserRole,
} from "../api/adminApi";
import { getErrorMessage } from "../api/axios";

// this const was missing before -> "btn is not defined" crash
const btn =
  "rounded-lg border border-slate-600 px-3 py-1 text-xs font-semibold cursor-pointer hover:bg-slate-700 disabled:opacity-40 disabled:cursor-default";
const searchInput =
  "w-full sm:w-72 rounded-lg bg-slate-800 border border-slate-600 px-3 py-2 text-sm outline-none focus:border-red-200";

function Stat({ label, value }) {
  return (
    <div className="rounded-xl bg-slate-800 border border-slate-700 p-4">
      <p className="text-2xl font-bold text-red-200">{value}</p>
      <p className="text-sm text-slate-400">{label}</p>
    </div>
  );
}

export default function AdminPage() {
  const { user: authUser } = useAuth();
  const dispatch = useDispatch();
  const skills = useSelector((s) => s.skills.items);
  const {
    data: usersData,
    error: loadError,
    refetch: refetchUsers,
  } = useFetch(fetchUsers);
  const users = useMemo(() => usersData ?? [], [usersData]);

  const [actionError, setActionError] = useState("");
  const [notice, setNotice] = useState("");
  const [busyKey, setBusyKey] = useState(null);
  const [userQuery, setUserQuery] = useState("");
  const [skillQuery, setSkillQuery] = useState("");
  const error = actionError || loadError;

  useEffect(() => {
    dispatch(loadSkills());
  }, [dispatch]);

  const run = async (key, fn, doneMessage, { reloadSkills = false } = {}) => {
    setActionError("");
    setNotice("");
    setBusyKey(key);
    try {
      await fn();
      refetchUsers();
      if (reloadSkills) dispatch(loadSkills(true));
      setNotice(doneMessage);
    } catch (e) {
      setActionError(getErrorMessage(e));
    } finally {
      setBusyKey(null);
    }
  };

  const isInactive = (u) => u.active === false;

  const toggleActive = (u) => {
    if (
      !isInactive(u) &&
      !window.confirm(
        `Deactivate ${u.name}? They will be signed out and can't log in.`,
      )
    )
      return;
    run(
      `user-${u.id}`,
      () => (isInactive(u) ? activateUser(u.id) : deactivateUser(u.id)),
      `${u.name} ${isInactive(u) ? "activated" : "deactivated"}`,
    );
  };

  const toggleRole = (u) => {
    const next = u.role === "ADMIN" ? "USER" : "ADMIN";
    run(
      `user-${u.id}`,
      () => setUserRole(u.id, next),
      `${u.name} is now ${next}`,
    );
  };

  const handleCredits = (u) => {
    const n = Number(window.prompt(`Credits to add to ${u.name}:`));
    if (Number.isInteger(n) && n > 0)
      run(
        `user-${u.id}`,
        () => addCredits(u.id, n),
        `Added ${n} credits to ${u.name}`,
      );
  };

  const handleDeleteSkill = (s) => {
    if (!window.confirm(`Delete "${s.name}"?`)) return;
    run(`skill-${s.id}`, () => deleteSkill(s.id), `"${s.name}" deleted`, {
      reloadSkills: true,
    });
  };

  const activeCount = users.filter((u) => !isInactive(u)).length;

  const shownUsers = useMemo(() => {
    const q = userQuery.trim().toLowerCase();
    return q
      ? users.filter(
          (u) =>
            u.name?.toLowerCase().includes(q) ||
            u.email?.toLowerCase().includes(q),
        )
      : users;
  }, [users, userQuery]);

  const shownSkills = useMemo(() => {
    const q = skillQuery.trim().toLowerCase();
    return q ? skills.filter((s) => s.name.toLowerCase().includes(q)) : skills;
  }, [skills, skillQuery]);

  return (
    <main className="max-w-5xl mx-auto px-4 pt-10 pb-16 space-y-10">
      <h1 className="text-2xl font-bold">Admin dashboard</h1>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <Stat label="Users" value={users.length} />
        <Stat label="Active" value={activeCount} />
        <Stat label="Deactivated" value={users.length - activeCount} />
        <Stat label="Skills" value={skills.length} />
      </div>

      {error && (
        <p className="rounded-lg bg-red-900/50 border border-red-500 px-3 py-2 text-sm">
          {error}
        </p>
      )}
      {notice && !error && (
        <p className="rounded-lg bg-green-900/40 border border-green-500 px-3 py-2 text-sm">
          {notice}
        </p>
      )}

      <section>
        <div className="flex flex-wrap items-center justify-between gap-3 mb-3">
          <h2 className="text-xl font-semibold">
            Users ({shownUsers.length}
            {userQuery ? ` of ${users.length}` : ""})
          </h2>
          <input
            className={searchInput}
            placeholder="Search name or email"
            value={userQuery}
            onChange={(e) => setUserQuery(e.target.value)}
          />
        </div>

        <div className="overflow-x-auto rounded-xl border border-slate-700">
          <table className="w-full text-sm">
            <thead className="bg-slate-800 text-left text-slate-400">
              <tr>
                <th className="p-3">Name</th>
                <th className="p-3">Email</th>
                <th className="p-3">Role</th>
                <th className="p-3">Credits</th>
                <th className="p-3">Status</th>
                <th className="p-3">Actions</th>
              </tr>
            </thead>
            <tbody>
              {shownUsers.map((u) => {
                const isMe = u.email === authUser.email;
                const busy = busyKey === `user-${u.id}`;
                return (
                  <tr key={u.id} className="border-t border-slate-700">
                    <td className="p-3">{u.name}</td>
                    <td className="p-3">{u.email}</td>
                    <td className="p-3">{u.role}</td>
                    <td className="p-3">{u.credits}</td>
                    <td className="p-3">
                      <span
                        className={`rounded-full px-2 py-0.5 text-xs font-semibold ${
                          isInactive(u)
                            ? "bg-red-900/60 text-red-200"
                            : "bg-green-900/60 text-green-200"
                        }`}
                      >
                        {isInactive(u) ? "Inactive" : "Active"}
                      </span>
                    </td>
                    <td className="p-3">
                      <div className="flex flex-wrap gap-2">
                        <button
                          className={btn}
                          disabled={busy}
                          onClick={() => handleCredits(u)}
                        >
                          + Credits
                        </button>
                        {isMe ? (
                          <span className="text-xs text-slate-500 self-center">
                            You
                          </span>
                        ) : (
                          <>
                            <button
                              className={btn}
                              disabled={busy}
                              onClick={() => toggleActive(u)}
                            >
                              {isInactive(u) ? "Activate" : "Deactivate"}
                            </button>
                            <button
                              className={btn}
                              disabled={busy}
                              onClick={() => toggleRole(u)}
                            >
                              Make {u.role === "ADMIN" ? "USER" : "ADMIN"}
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
              {shownUsers.length === 0 && (
                <tr>
                  <td colSpan={6} className="p-4 text-slate-500">
                    No users match.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      <section>
        <div className="flex flex-wrap items-center justify-between gap-3 mb-3">
          <h2 className="text-xl font-semibold">
            Skills ({shownSkills.length}
            {skillQuery ? ` of ${skills.length}` : ""})
          </h2>
          <input
            className={searchInput}
            placeholder="Search skills"
            value={skillQuery}
            onChange={(e) => setSkillQuery(e.target.value)}
          />
        </div>
        <ul className="grid grid-cols-2 sm:grid-cols-3 gap-3">
          {shownSkills.map((s) => (
            <li
              key={s.id}
              className="rounded-xl bg-slate-800 border border-slate-700 p-3 flex justify-between items-center gap-2"
            >
              <span className="truncate">{s.name}</span>
              <button
                className={btn}
                disabled={busyKey === `skill-${s.id}`}
                onClick={() => handleDeleteSkill(s)}
              >
                Delete
              </button>
            </li>
          ))}
        </ul>
        {shownSkills.length === 0 && (
          <p className="text-slate-500 text-sm">No skills match.</p>
        )}
      </section>
    </main>
  );
}
