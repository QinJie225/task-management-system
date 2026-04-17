import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import App from "./App";
import "./index.css";
import { AuthProvider } from "react-oauth2-code-pkce";  
import { authConfig } from "./auth/authConfig.js"; 

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <AuthProvider authConfig={authConfig}>
      <App />
    </AuthProvider>
  </StrictMode>,
);
