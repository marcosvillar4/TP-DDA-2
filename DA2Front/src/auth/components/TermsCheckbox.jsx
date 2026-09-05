import "..styles/TermsCheckbox.css";

export function TermsCheckbox({ id, checked, onChange, onTermsClick }) {
  return (
    <label htmlFor={id} className="terms-checkbox">
      <input
        id={id}
        type="checkbox"
        checked={checked}
        onChange={onChange}
        className="terms-checkbox-input"
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
          className="terms-checkbox-link"
        >
          términos y condiciones
        </button>
      </span>
    </label>
  );
}