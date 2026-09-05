export function LinkText({ children, ...props }) {
  return (
    <button
      type="button"
      className="text-sm font-medium text-[#16223f] hover:text-[#d6273c] hover:underline"
      {...props}
    >
      {children}
    </button>
  );
}