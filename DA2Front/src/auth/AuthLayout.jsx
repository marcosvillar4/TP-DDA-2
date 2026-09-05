export function AuthLayout({ children }) {
  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden bg-slate-100 px-4 py-10">
      <div className="pointer-events-none absolute -left-24 -bottom-24 h-72 w-72 rounded-full bg-rose-100/70" />
      <div className="pointer-events-none absolute -right-24 -top-24 h-72 w-72 rounded-full bg-slate-200/80" />
      <div className="relative z-10 w-full max-w-md">{children}</div>
    </div>
  );
}
 