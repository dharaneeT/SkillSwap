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
import Icon from "../components/Icon";
import SkillModeration from "../components/admin/SkillModeration"; // NEW

// this const was missing before -> "btn is not defined" crash
const btn = "btn btn-secondary btn-sm";
const searchInput = "input input-with-icon";

// tone = one of the pastel card classes
function Stat({ label, value, tone, icon }) {
  return (
    <div className={`card ${tone} flex items-start justify-between gap-3`}>
      <div>
        <p className="text-3xl font-extrabold leading-none tracking-tight">
          {value}
        </p>
        <p className="mt-2 text-sm opacity-75">{label}</p>
      </div>
      <Icon name={icon} size={20} />
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
    <main className="page max-w-6xl space-y-10 pb-16">
      <div className="space-y-2">
        <p className="eyebrow">Admin</p>
        <h1 className="page-title">Admin dashboard</h1>
      </div>

      <div className="grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-4">
        <Stat
          label="Users"
          value={users.length}
          tone="card-lavender"
          icon="users"
        />
        <Stat
          label="Active"
          value={activeCount}
          tone="card-periwinkle"
          icon="circle-check"
        />
        <Stat
          label="Deactivated"
          value={users.length - activeCount}
          tone=""
          icon="ban"
        />
        <Stat
          label="Skills"
          value={skills.length}
          tone="card-navy"
          icon="book-open"
        />
      </div>

      {error && (
        <p role="alert" className="alert alert-error">
          <Icon name="alert-circle" size={18} className="mt-0.5" />
          <span>{error}</span>
        </p>
      )}
      {notice && !error && (
        <p role="status" className="alert alert-success">
          <Icon name="circle-check" size={18} className="mt-0.5" />
          <span>{notice}</span>
        </p>
      )}

      {/* NEW: approve new skills / merge duplicates */}
      <SkillModeration onChanged={() => dispatch(loadSkills(true))} />

      <section>
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <h2 className="section-title flex items-center gap-2">
            <Icon name="users" size={20} />
            Users ({shownUsers.length}
            {userQuery ? ` of ${users.length}` : ""})
          </h2>
          <div className="relative w-full sm:w-72">
            <Icon name="search" size={16} className="input-icon" />
            <input
              className={searchInput}
              placeholder="Search name or email"
              aria-label="Search users by name or email"
              value={userQuery}
              onChange={(e) => setUserQuery(e.target.value)}
            />
          </div>
        </div>

        <div className="overflow-x-auto rounded-[1.75rem] border border-line bg-white">
          <table className="w-full text-sm">
            <thead className="bg-surface text-left text-xs font-semibold uppercase tracking-wider text-muted">
              <tr>
                <th className="p-4">Name</th>
                <th className="p-4">Email</th>
                <th className="p-4">Role</th>
                <th className="p-4">Credits</th>
                <th className="p-4">Status</th>
                <th className="p-4">Actions</th>
              </tr>
            </thead>
            <tbody>
              {shownUsers.map((u) => {
                const isMe = u.email === authUser.email;
                const busy = busyKey === `user-${u.id}`;
                return (
                  <tr
                    key={u.id}
                    className="border-t border-line transition-colors hover:bg-cream"
                  >
                    <td className="p-4 font-semibold">{u.name}</td>
                    <td className="p-4 text-muted">{u.email}</td>
                    <td className="p-4">
                      <span
                        className={`badge ${
                          u.role === "ADMIN" ? "badge-accepted" : ""
                        }`}
                      >
                        {u.role}
                      </span>
                    </td>
                    <td className="p-4">{u.credits}</td>
                    <td className="p-4">
                      <span
                        className={`badge ${
                          isInactive(u) ? "badge-danger" : "badge-success"
                        }`}
                      >
                        {isInactive(u) ? "Inactive" : "Active"}
                      </span>
                    </td>
                    <td className="p-4">
                      <div className="flex flex-wrap gap-2">
                        <button
                          className={btn}
                          disabled={busy}
                          onClick={() => handleCredits(u)}
                        >
                          <Icon name="plus" size={13} />
                          Credits
                        </button>
                        {isMe ? (
                          <span className="meta self-center">You</span>
                        ) : (
                          <>
                            <button
                              className={btn}
                              disabled={busy}
                              onClick={() => toggleActive(u)}
                            >
                              <Icon
                                name={isInactive(u) ? "circle-check" : "ban"}
                                size={13}
                              />
                              {isInactive(u) ? "Activate" : "Deactivate"}
                            </button>
                            <button
                              className={btn}
                              disabled={busy}
                              onClick={() => toggleRole(u)}
                            >
                              <Icon name="shield-check" size={13} />
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
                  <td colSpan={6} className="p-5 text-muted">
                    No users match.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      <section>
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <h2 className="section-title flex items-center gap-2">
            <Icon name="book-open" size={20} />
            Skills ({shownSkills.length}
            {skillQuery ? ` of ${skills.length}` : ""})
          </h2>
          <div className="relative w-full sm:w-72">
            <Icon name="search" size={16} className="input-icon" />
            <input
              className={searchInput}
              placeholder="Search skills"
              aria-label="Search skills"
              value={skillQuery}
              onChange={(e) => setSkillQuery(e.target.value)}
            />
          </div>
        </div>
        <ul className="grid grid-cols-1 gap-3 min-[480px]:grid-cols-2 lg:grid-cols-3">
          {shownSkills.map((s) => (
            <li
              key={s.id}
              className="card card-white flex items-center justify-between gap-2 py-3 sm:py-3"
            >
              <span className="truncate font-medium">{s.name}</span>
              <button
                className={btn}
                disabled={busyKey === `skill-${s.id}`}
                onClick={() => handleDeleteSkill(s)}
                aria-label={`Delete ${s.name}`}
              >
                <Icon name="trash-2" size={13} />
                Delete
              </button>
            </li>
          ))}
        </ul>
        {shownSkills.length === 0 && (
          <p className="text-muted text-sm">No skills match.</p>
        )}
      </section>
    </main>
  );
}
