import "./ForbiddenPage.css";
import { Link } from "react-router-dom";

export function ForbiddenPage() {
  
  return (
    <div className="not-found-container">
      <title>403 Forbidden</title>

      <div className="not-found-content">
        <h1 className="error-code">403</h1>
        <h2 className="error-title">Forbidden</h2>
        <p className="error-text">
          Sorry, the page you’re looking for is forbidden.
        </p>

        <Link to="/" className="home-button">
          Go Back Home
        </Link>
      </div>
    </div>
  );
}