import "../styles/LinkText.css";
 
export function LinkText({ children, ...props }) {
  return (
    <button type="button" className="link-text" {...props}>
      {children}
    </button>
  );
}