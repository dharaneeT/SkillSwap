import { useEffect, useState } from "react";
import { searchSkills } from "../api/skillApi";
import { useFetch } from "../hooks/useFetch";
import Icon from "../components/Icon";

const PAGE_SIZE = 12;

const field = "input select";

export default function SkillsListPage() {
  const [search, setSearch] = useState("");
  const [debounced, setDebounced] = useState("");
  const [type, setType] = useState("");
  const [minRating, setMinRating] = useState("");
  const [sort, setSort] = useState("name,asc");
  const [page, setPage] = useState(0);

  // Wait 300 ms after typing stops before searching
  useEffect(() => {
    const t = setTimeout(() => {
      setDebounced(search.trim());
      setPage(0);
    }, 300);

    return () => clearTimeout(t);
  }, [search]);

  const params = {
    page,
    size: PAGE_SIZE,
    sort,
    ...(debounced && { search: debounced }),
    ...(type && { type }),
    ...(minRating && { minRating }),
  };

  const { data, error, loading } = useFetch(searchSkills, params);

  return (
    <main className="page max-w-5xl">
      <div className="mb-8 space-y-2">
        <p className="eyebrow">Browse</p>
        <h1 className="page-title">Skills</h1>
      </div>

      <div className="mb-8 flex flex-wrap items-center gap-3">
        <div className="relative min-w-52 flex-1">
          <Icon name="search" size={17} className="input-icon" />
          <input
            className="input input-with-icon"
            placeholder="Search skills..."
            aria-label="Search skills"
            value={search}
            maxLength={50}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <span className="hidden items-center text-muted sm:flex" aria-hidden="true">
          <Icon name="sliders" size={18} />
        </span>

        <select
          className={field}
          aria-label="Offered or wanted"
          value={type}
          onChange={(e) => {
            setType(e.target.value);
            setPage(0);
          }}
        >
          <option value="">Offered or wanted</option>
          <option value="OFFERED">Offered by someone</option>
          <option value="WANTED">Wanted by someone</option>
        </select>

        <select
          className={field}
          aria-label="Provider rating"
          value={minRating}
          onChange={(e) => {
            setMinRating(e.target.value);
            setPage(0);
          }}
        >
          <option value="">Any provider rating</option>
          <option value="3">Providers 3★ and up</option>
          <option value="4">Providers 4★ and up</option>
        </select>

        <select
          className={field}
          aria-label="Sort"
          value={sort}
          onChange={(e) => {
            setSort(e.target.value);
            setPage(0);
          }}
        >
          <option value="name,asc">Name A–Z</option>
          <option value="name,desc">Name Z–A</option>
          <option value="id,desc">Newest first</option>
        </select>
      </div>

      {error && !loading && (
        <p role="alert" className="alert alert-error mb-6">
          <Icon name="alert-circle" size={18} className="mt-0.5" />
          <span>{error}</span>
        </p>
      )}

      {loading && (
        <p className="flex items-center gap-2 text-muted">
          <Icon name="search" size={16} />
          Searching...
        </p>
      )}

      {!loading && data && data.content.length === 0 && (
        <p className="card flex items-center gap-2 text-sm text-muted">
          <Icon name="search" size={16} />
          No skills match your filters.
        </p>
      )}

      {data && (
        <ul className="grid grid-cols-1 gap-4 min-[480px]:grid-cols-2 lg:grid-cols-3">
          {data.content.map((s, i) => (
            <li
              key={s.id}
              className={`card flex min-h-36 flex-col justify-between gap-6 ${
                i % 2 === 0 ? "card-lavender" : "card-periwinkle"
              }`}
            >
              <span className="flex h-10 w-10 items-center justify-center rounded-full bg-white">
                <Icon name="book-open" size={18} />
              </span>
              <div>
                <p className="card-title text-lg">{s.name}</p>
                <p className="meta mt-1 text-ink/70">
                  {s.offeredCount} offering · {s.wantedCount} wanting
                </p>
              </div>
            </li>
          ))}
        </ul>
      )}

      {data && data.totalPages > 1 && (
        <div className="mt-8 flex flex-wrap items-center justify-between gap-3 text-sm">
          <button
            disabled={!data.hasPrevious}
            onClick={() => setPage((p) => p - 1)}
            className="btn btn-secondary btn-sm"
          >
            <Icon name="chevron-left" size={15} />
            Prev
          </button>

          <span className="meta">
            Page {data.page + 1} of {data.totalPages} · {data.totalElements}{" "}
            skills
          </span>

          <button
            disabled={!data.hasNext}
            onClick={() => setPage((p) => p + 1)}
            className="btn btn-secondary btn-sm"
          >
            Next
            <Icon name="chevron-right" size={15} />
          </button>
        </div>
      )}
    </main>
  );
}
