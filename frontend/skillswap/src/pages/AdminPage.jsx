import { useEffect, useState } from "react";
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

export default function AdminPage() {
  const { user: authUser } = useAuth();
  const dispatch = useDispatch();
  const skills = useSelector((s) => s.skills.items);
  const {
    data: usersData,
    error: loadError,
    refetch: refetchUsers,
  } = useFetch(fetchUsers);
  const users = usersData ?? [];
  const [actionError, setActionError] = useState("");
  const error = actionError || loadError;

  useEffect(() => {
    dispatch(loadSkills());
  }, [dispatch]);

  const run = async (fn, { reloadSkills = false } = {}) => {
    setActionError("");
    try {
      await fn();
      refetchUsers();
      if (reloadSkills) dispatch(loadSkills(true));
    } catch (e) {
      setActionError(getErrorMessage(e));
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
                  run(() => deleteSkill(id), { reloadSkills: true })
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
