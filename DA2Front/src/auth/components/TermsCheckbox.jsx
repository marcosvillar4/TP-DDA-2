export function TermsCheckbox({ id, checked, onChange, onTermsClick }) {
  return (
    <label
      htmlFor={id}
      className="flex items-start gap-2 text-sm text-slate-600"
    >
      <input
        id={id}
        type="checkbox"
        checked={checked}
        onChange={onChange}
        className="mt-0.5 h-4 w-4 shrink-0 rounded border-slate-300 text-[#16223f] focus:ring-[#16223f]/30"
      />
      <span>
        Leí y estoy de acuerdo con los{" "}
        <button
          type="button"
          onClick={(e) => {
            e.preventDefault();
            e.stopPropagation();
            onTermsClick?.();
          }}
          className="font-medium text-[#16223f] hover:text-[#d6273c] hover:underline"
        >
          términos y condiciones
        </button>
      </span>
    </label>
  );
}