import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.jsx";
import { createAppRouter } from "./routes/router.jsx";
import keycloak from './keycloak.js'


const root = createRoot(document.getElementById("root"));

root.render(<div className="loading">Authenticating...</div>);

keycloak
  .init({
    onLoad: "login-required",
    checkLoginIframe: false,
    pkceMethod: "S256", 
  })
  .then((authenticated) => {
    if (authenticated) {
      const router = createAppRouter();
      root.render(
        <StrictMode>
          <App router={router} />
        </StrictMode>,
      );
    }
  })
  .catch((err) => {
    console.error("Keycloak Init Error", err);
    root.render(
      <div>Authentication failed. Check if Keycloak is on 8080.</div>,
    );
  });

keycloak.onTokenExpired = () => {
  keycloak.updateToken(30); 
};
