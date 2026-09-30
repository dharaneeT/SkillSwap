import { useCallback, useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { fetchSkills } from "../api/skillApi";
import {
  activateUser,
  addCredits,
  deactivateUser,
  deleteSkill,
  fetchUsers,
  setUserRole,
} from "../api/adminApi";
import { getErrorMessage } from "../api/axios";

const btn =
  "rounded-lg border border-slate-600 px-2 py-1 text-xs cursor-pointer hover:bg-slate-700";

export default function AdminPage() {
  const { user: authUser } = useAuth();
  const [users, setUsers] = useState([]);
  const [skills, setSkills] = useState([]);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    const [u, s] = await Promise.all([fetchUsers(), fetchSkills()]);
    setUsers(u.data.data);
    setSkills(s.data.data);
  }, []);

  useEffect(() => {
    load().catch((e) => setError(getErrorMessage(e)));
  }, [load]);

  // run an admin action, then refresh the tables
  const run = async (fn) => {
    setError("");
    try {
      await fn();
      await load();
    } catch (e) {
      setError(getErrorMessage(e));
    }
  };

  const handleCredits = (u) => {
    const n = Number(window.prompt(`Credits to add to ${u.name}:`));
    if (Number.isInteger(n) && n > 0) run(() => addCredits(u.id, n));
  };

  return (
    <main className="max-w-5xl mx-auto px-4 pt-10 space-y-10">
      <h1 className="text-2xl font-bold">Admin</h1>
      {error && (
        <p className="rounded-lg bg-red-900/50 border border-red-500 px-3 py-2 text-sm">
          {error}
        </p>
      )}

      <section>
        <h2 className="text-xl font-semibold mb-3">Users ({users.length})</h2>
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
              {users.map((u) => {
                const isMe = u.email === authUser.email; // backend blocks self-changes too
                return (
                  <tr key={u.id} className="border-t border-slate-700">
                    <td className="p-3">{u.name}</td>
                    <td className="p-3">{u.email}</td>
                    <td className="p-3">{u.role}</td>
                    <td className="p-3">{u.credits}</td>
                    <td className="p-3">
                      {u.active === false ? "Inactive" : "Active"}
                    </td>
                    <td className="p-3 flex flex-wrap gap-2">
                      <button className={btn} onClick={() => handleCredits(u)}>
                        + Credits
                      </button>
                      {!isMe && (
                        <>
                          <button
                            className={btn}
                            onClick={() =>
                              run(() =>
                                u.active === false
                                  ? activateUser(u.id)
                                  : deactivateUser(u.id),
                              )
                            }
                          >
                            {u.active === false ? "Activate" : "Deactivate"}
                          </button>
                          <button
                            className={btn}
                            onClick={() =>
                              run(() =>
                                setUserRole(
                                  u.id,
                                  u.role === "ADMIN" ? "USER" : "ADMIN",
                                ),
                              )
                            }
                          >
                            Make {u.role === "ADMIN" ? "USER" : "ADMIN"}
                          </button>
                        </>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </section>

      <section>
        <h2 className="text-xl font-semibold mb-3">Skills ({skills.length})</h2>
        <ul className="grid grid-cols-2 sm:grid-cols-3 gap-3">
          {skills.map((s) => (
            <li
              key={s.id}
              className="rounded-xl bg-slate-800 border border-slate-700 p-3 flex justify-between items-center"
            >
              {s.name}
              <button
                className={btn}
                onClick={() =>
                  window.confirm(`Delete "${s.name}"?`) &&
                  run(() => deleteSkill(s.id))
                }
              >
                Delete
              </button>
            </li>
          ))}
        </ul>
      </section>
    </main>
  );
}
