// pages/CertificatesPage.jsx  (NEW FILE)  route: /certificates
import { Link } from "react-router-dom";
import { fetchMyCertificates } from "../api/progressApi";
import { useFetch } from "../hooks/useFetch";
import Icon from "../components/Icon";

export default function CertificatesPage() {
  const { data, error, loading } = useFetch(fetchMyCertificates);
  const certs = data ?? [];

  return (
    <main className="page max-w-4xl space-y-8">
      <header className="flex flex-col gap-2">
        <p className="eyebrow">Achievements</p>
        <h1 className="page-title">My certificates</h1>
        <p className="page-sub max-w-xl">
          You earn a certificate when you complete 3 sessions of the same skill
          with the same teacher.
        </p>
      </header>

      {error && (
        <p role="alert" className="alert alert-error">
          <Icon name="alert-circle" size={18} className="mt-0.5" />
          <span>{error}</span>
        </p>
      )}
      {!data && loading && <p className="text-sm text-muted">Loading...</p>}

      {data && certs.length === 0 && (
        <div className="card flex flex-wrap items-center justify-between gap-3">
          <p className="flex items-center gap-2 text-sm text-muted">
            <Icon name="award" size={16} />
            No certificates yet. Keep learning, you&apos;re closer than you
            think.
          </p>
          <Link to="/matches" className="btn btn-primary btn-sm">
            Find a teacher
          </Link>
        </div>
      )}

      <ul className="grid gap-3 sm:grid-cols-2">
        {certs.map((c) => (
          <li key={c.code} className="card card-white flex flex-col gap-3">
            <span className="medal medal-gold">
              <Icon name="award" size={21} />
            </span>
            <div className="min-w-0">
              <p className="card-title truncate">{c.skillName}</p>
              <p className="meta mt-1">
                with {c.providerName} · {c.sessionsCompleted} sessions
              </p>
              <p className="meta mt-0.5">
                {new Date(c.issuedAt).toLocaleDateString([], {
                  dateStyle: "medium",
                })}
              </p>
            </div>
            <Link
              to={`/certificates/${c.code}`}
              className="btn btn-secondary btn-sm self-start"
            >
              View &amp; print
              <Icon name="arrow-right" size={14} />
            </Link>
          </li>
        ))}
      </ul>
    </main>
  );
}
