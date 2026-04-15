import "./NotFoundPage.css";
import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <div className="not-found-container">
      <title>404 Page Not Found</title>

      <div className="not-found-content">
        <h1 className="error-code">404</h1>
        <h2 className="error-title">Page Not Found</h2>
        <p className="error-text">
          Sorry, the page you’re looking for doesn’t exist or has been moved.
        </p>

        <Link to="/" className="home-button">
          Go Back Home
        </Link>
      </div>
    </div>
  );
}