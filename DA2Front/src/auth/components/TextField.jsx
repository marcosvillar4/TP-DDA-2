export function TextField({ label, id, ...props }) {
  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-sm font-medium text-slate-700">
        {label}
      </label>
      <input
        id={id}
        className="w-full rounded-lg border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm text-slate-800 placeholder:text-slate-400 outline-none transition focus:border-[#16223f] focus:bg-white focus:ring-2 focus:ring-[#16223f]/10"
        {...props}
      />
    </div>
  );
}