import { Link } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import Icon from "../components/Icon";

export default function HomePage() {
  const { user } = useAuth();
  return (
    <main className="page max-w-6xl space-y-12 sm:space-y-16">
      <section className="mx-auto max-w-3xl space-y-6 pt-6 text-center sm:pt-14">
        <p className="eyebrow inline-flex items-center gap-2 rounded-full bg-lavender px-3 py-1 text-ink">
          <Icon name="repeat" size={14} />
          Skill exchange
        </p>
        <h1 className="page-title text-balance sm:text-6xl">
          Trade skills, not money.
        </h1>
        <p className="page-sub mx-auto max-w-xl">
          List what you can teach, what you want to learn, and get matched with
          people who complete the swap. Sessions are paid in credits.
        </p>
        <div className="flex flex-wrap justify-center gap-3 pt-2">
          {user ? (
            <Link to="/dashboard" className="btn btn-primary">
              Go to dashboard
              <Icon name="arrow-right" size={16} />
            </Link>
          ) : (
            <>
              <Link to="/signup" className="btn btn-primary">
                Get started
                <Icon name="arrow-right" size={16} />
              </Link>
              <Link to="/login" className="btn btn-secondary">
                <Icon name="log-in" size={16} />
                Log in
              </Link>
            </>
          )}
        </div>
      </section>

      <section
        aria-label="How it works"
        className="grid gap-4 md:grid-cols-3"
      >
        <article className="card card-lavender flex flex-col gap-10">
          <span className="flex h-11 w-11 items-center justify-center rounded-full bg-white">
            <Icon name="graduation-cap" size={20} />
          </span>
          <div>
            <h2 className="card-title text-xl">List your skills</h2>
            <p className="mt-1 text-sm text-ink/70">
              Add what you can teach and what you want to learn.
            </p>
          </div>
        </article>
        <article className="card card-periwinkle flex flex-col gap-10">
          <span className="flex h-11 w-11 items-center justify-center rounded-full bg-white">
            <Icon name="users" size={20} />
          </span>
          <div>
            <h2 className="card-title text-xl">Get matched</h2>
            <p className="mt-1 text-sm text-ink/70">
              Meet people who complete the swap.
            </p>
          </div>
        </article>
        <article className="card card-navy flex flex-col gap-10">
          <span className="flex h-11 w-11 items-center justify-center rounded-full bg-white text-navy">
            <Icon name="coins" size={20} />
          </span>
          <div>
            <h2 className="card-title text-xl">Pay in credits</h2>
            <p className="mt-1 text-sm text-white/75">
              Sessions are paid in credits, not money.
            </p>
          </div>
        </article>
      </section>
    </main>
  );
}
