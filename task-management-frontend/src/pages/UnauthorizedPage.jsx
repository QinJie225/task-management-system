import "./UnauthorizedPage.css";
import { getAuthData } from "../utils/auth";

export function UnauthorizedPage() {
  const { logout } = getAuthData();
  return (
    <div className="not-found-container">
      <title>401 Unauthorized</title>

      <div className="not-found-content">
        <h1 className="error-code">401</h1>
        <h2 className="error-title">Unauthorized</h2>
        <p className="error-text">Sorry, you are not authorized</p>

        <button onClick={logout}>Log Out</button>
      </div>
    </div>
  );
}
