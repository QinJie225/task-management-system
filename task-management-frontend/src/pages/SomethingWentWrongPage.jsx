import { Link } from "react-router-dom";
import "./SomethingWentWrongPage.css";
import { useAuthData } from "../hooks/useAuthData";

export function SomethingWentWrongPage({ error }) {
  const { logout } = useAuthData();
  return (
    <div className="something-went-wrong-content">
      <h1>Something Went Wrong</h1>
      <p>{error?.message ?? "An unexpected error occurred."}</p>

      <button onClick={logout}>Log Out</button>
    </div>
  );
}
