import { useAuthData } from "../hooks/useAuthData";
import "./ForbiddenPage.css";
import { Link } from "react-router-dom";

export function ForbiddenPage() {
  const { isAuthorized, logout } = useAuthData();

  return (
    <div className="not-found-container">
      <title>403 Forbidden</title>

      <div className="not-found-content">
        <h1 className="error-code">403</h1>
        <h2 className="error-title">Forbidden</h2>
        {!isAuthorized ? (
          <>
            <p className="error-text">
              You are authenticated, but you do not have the required roles to
              access this application.
            </p>

            <button onClick={logout} className="home-button">
              Log Out
            </button>
          </>
        ) : (
          <>
            <p className="error-text">
              Sorry, the page you’re looking for is forbidden.
            </p>

            <Link to="/" className="home-button">
              Go Back Home
            </Link>
          </>
        )}
      </div>
    </div>
  );
}
