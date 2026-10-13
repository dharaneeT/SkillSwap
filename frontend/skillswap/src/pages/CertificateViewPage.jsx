// pages/CertificateViewPage.jsx  (NEW FILE)  route: /certificates/:code
// Anyone logged in can open a code to verify it; the server only returns it if it is real.
import { Link, useParams } from "react-router-dom";
import { fetchCertificate } from "../api/progressApi";
import { useFetch } from "../hooks/useFetch";
import Icon from "../components/Icon";

// hide the site header + buttons when printing / saving as PDF
const PRINT_CSS = `@media print {
  header.sticky, .no-print { display: none !important; }
  body, html { background: #fff !important; }
  main.page { padding: 0 !important; }
  .cert { box-shadow: none !important; }
}`;

export default function CertificateViewPage() {
  const { code } = useParams();
  const { data: c, error, loading } = useFetch(fetchCertificate, code);

  if (error) {
    return (
      <main className="page max-w-3xl space-y-4">
        <p role="alert" className="alert alert-error">
          <Icon name="alert-circle" size={18} className="mt-0.5" />
          <span>{error}</span>
        </p>
        <Link to="/certificates" className="btn btn-secondary btn-sm">
          <Icon name="chevron-left" size={14} />
          Back
        </Link>
      </main>
    );
  }
  if (!c || loading) {
    return (
      <main className="page max-w-3xl">
        <p className="text-sm text-muted">Loading...</p>
      </main>
    );
  }

  const date = new Date(c.issuedAt).toLocaleDateString([], {
    dateStyle: "long",
  });

  return (
    <main className="page max-w-3xl space-y-6">
      <style>{PRINT_CSS}</style>

      <div className="no-print flex flex-wrap items-center justify-between gap-3">
        <Link to="/certificates" className="btn btn-secondary btn-sm">
          <Icon name="chevron-left" size={14} />
          All certificates
        </Link>
        <button
          type="button"
          onClick={() => window.print()}
          className="btn btn-primary btn-sm"
        >
          <Icon name="send" size={14} />
          Print / save as PDF
        </button>
      </div>

      <section
        aria-label="Certificate of completion"
        className="cert rounded-[1.75rem] border-4 border-double border-navy bg-white p-6 text-center sm:p-12"
      >
        <span className="medal medal-lg medal-gold mx-auto">
          <Icon name="award" size={30} />
        </span>
        <p className="eyebrow mt-5">SkillSwap</p>
        <h1 className="mt-2 font-serif text-3xl font-bold tracking-tight sm:text-5xl">
          Certificate of Completion
        </h1>

        <p className="mt-8 text-muted">This certifies that</p>
        <p className="mt-2 break-words font-serif text-3xl font-bold text-navy sm:text-4xl">
          {c.learnerName}
        </p>
        <p className="mt-6 text-muted">has successfully completed</p>
        <p className="mt-2 break-words text-2xl font-extrabold sm:text-3xl">
          {c.sessionsCompleted} sessions of {c.skillName}
        </p>
        <p className="mt-2 text-muted">
          taught by{" "}
          <span className="font-semibold text-ink">{c.providerName}</span>
        </p>

        <div className="mt-10 grid gap-4 border-t border-line pt-6 text-sm sm:grid-cols-2 sm:text-left">
          <p>
            <span className="meta block">Date of last session</span>
            {date}
          </p>
          <p className="sm:text-right">
            <span className="meta block">Verification code</span>
            <span className="font-mono">{c.code}</span>
          </p>
        </div>
        <p className="meta mt-4 break-all">
          Verify at {window.location.origin}/certificates/{c.code}
        </p>
      </section>
    </main>
  );
}
