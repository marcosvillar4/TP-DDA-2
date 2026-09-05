export function AuthCard({ children }) {
  return (
    <div className="overflow-hidden rounded-2xl bg-white shadow-xl shadow-slate-900/5">
      <div className="h-1.5 bg-gradient-to-r from-[#16223f] via-[#5a1f33] to-[#d6273c]" />
      <div className="px-8 py-10 sm:px-10">{children}</div>
    </div>
  );
}