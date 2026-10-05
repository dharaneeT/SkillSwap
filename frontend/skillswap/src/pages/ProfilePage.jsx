import { useEffect, useState } from "react";
import { fetchMe } from "../api/userApi";
import { fetchSkills } from "../api/skillApi";
import { addUserSkillByName } from "../api/userSkillApi";
import { getErrorMessage } from "../api/axios";
import { fetchReviewsForUser } from "../api/reviewApi";

const inputClass =
  "rounded-lg bg-slate-800 border border-slate-600 px-3 py-2 outline-none focus:border-red-200";

function Chips({ items, empty }) {
  if (!items?.length) return <p className="text-slate-500 text-sm">{empty}</p>;
  return (
    <ul className="flex flex-wrap gap-2">
      {items.map((s, i) => (
        <li key={i} className="rounded-full bg-slate-700 px-3 py-1 text-sm">
          {s}
        </li>
      ))}
    </ul>
  );
}

export default function ProfilePage() {
  const [profile, setProfile] = useState(null);
  const [skills, setSkills] = useState([]);
  const [form, setForm] = useState({ name: "", type: "OFFERED" });
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [reviews, setReviews] = useState([]);

  useEffect(() => {
    let ignore = false;
    Promise.all([fetchMe(), fetchSkills()])
      .then(([me, sk]) => {
        if (ignore) return;
        setProfile(me.data.data);
        setSkills(sk.data.data);
      })
      .catch((err) => !ignore && setError(getErrorMessage(err)));
    return () => {
      ignore = true;
    };
  }, []);

  const profileId = profile?.id;
  useEffect(() => {
    if (!profileId) return;
    let ignore = false;
    fetchReviewsForUser(profileId)
      .then((res) => !ignore && setReviews(res.data.data.content))
      .catch(() => {}); // reviews are optional on this page
    return () => {
      ignore = true;
    };
  }, [profileId]);

  //
  const handleAdd = async (e) => {
    e.preventDefault();
    setError("");
    setNotice("");

    const name = form.name.trim();

    if (!name) {
      setError("Type the name of a skill");
      return;
    }

    try {
      await addUserSkillByName({
        name,
        type: form.type,
      });

      const [me, sk] = await Promise.all([fetchMe(), fetchSkills()]);

      setProfile(me.data.data);
      setSkills(sk.data.data);
      setForm({ ...form, name: "" });
      setNotice(`${name} added`);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  if (!profile) {
    return (
      <main className="max-w-3xl mx-auto px-4 pt-10">
        {error ? (
          <p className="text-red-300">{error}</p>
        ) : (
          <p className="text-slate-400">Loading...</p>
        )}
      </main>
    );
  }

  return (
    <main className="max-w-3xl mx-auto px-4 pt-10 space-y-8">
      <section className="rounded-xl bg-slate-800 border border-slate-700 p-5 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">{profile.name}</h1>
          <p className="text-slate-400">{profile.email}</p>
          <p className="text-sm text-slate-300 mt-1">
            {profile.reviewCount > 0
              ? `★ ${profile.averageRating.toFixed(1)} (${profile.reviewCount} reviews)`
              : "No reviews yet"}
          </p>
        </div>
        <div className="text-right">
          <p className="text-3xl font-bold text-red-200">{profile.credits}</p>
          <p className="text-slate-400 text-sm">credits</p>
        </div>
      </section>

      <section className="grid sm:grid-cols-2 gap-6">
        <div>
          <h2 className="font-semibold mb-2">I can teach</h2>
          <Chips items={profile.offeredSkills} empty="Nothing added yet" />
        </div>
        <div>
          <h2 className="font-semibold mb-2">I want to learn</h2>
          <Chips items={profile.wantedSkills} empty="Nothing added yet" />
        </div>
      </section>

      <form onSubmit={handleAdd} className="space-y-3">
        <h2 className="font-semibold">Add a skill</h2>
        {error && (
          <p className="rounded-lg bg-red-900/50 border border-red-500 px-3 py-2 text-sm">
            {error}
          </p>
        )}
        {notice && <p className="text-green-300 text-sm">{notice}</p>}
        <div className="flex flex-wrap gap-3">
          <input
            className={`${inputClass} flex-1 min-w-48`}
            list="skill-suggestions"
            placeholder="Type a skill, e.g. Guitar"
            value={form.name}
            maxLength={50}
            required
            onChange={(e) => setForm({ ...form, name: e.target.value })}
          />

          <datalist id="skill-suggestions">
            {skills.map((s) => (
              <option key={s.id} value={s.name} />
            ))}
          </datalist>
          <select
            className={inputClass}
            value={form.type}
            onChange={(e) => setForm({ ...form, type: e.target.value })}
          >
            <option value="OFFERED">I can teach this</option>
            <option value="WANTED">I want to learn this</option>
          </select>
          <button className="rounded-lg bg-red-200 text-slate-900 font-semibold px-4 py-2 cursor-pointer">
            Add
          </button>
        </div>
      </form>
      <p className="text-sm text-slate-300">
        {profile.reviewCount > 0
          ? `★ ${profile.averageRating.toFixed(1)} (${profile.reviewCount} reviews)`
          : "No reviews yet"}
      </p>

      <section>
        <h2 className="font-semibold mb-2">Reviews I've received</h2>

        {reviews.length === 0 && (
          <p className="text-slate-500 text-sm">No reviews yet</p>
        )}

        <ul className="space-y-3">
          {reviews.map((r) => (
            <li
              key={r.id}
              className="rounded-xl bg-slate-800 border border-slate-700 p-4"
            >
              <p className="text-yellow-300">
                {"★".repeat(r.rating)}
                <span className="text-slate-600">
                  {"★".repeat(5 - r.rating)}
                </span>
              </p>

              {r.comment && <p className="text-sm mt-1">{r.comment}</p>}

              <p className="text-xs text-slate-400 mt-1">— {r.name}</p>
            </li>
          ))}
        </ul>
      </section>
    </main>
  );
}
