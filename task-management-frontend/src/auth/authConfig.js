export const authConfig = {
  clientId: "task-frontend",
  authorizationEndpoint:
    "http://localhost:8080/realms/internship-task-realm/protocol/openid-connect/auth",
  tokenEndpoint:
    "http://localhost:8080/realms/internship-task-realm/protocol/openid-connect/token",
  logoutEndpoint:
    "http://localhost:8080/realms/internship-task-realm/protocol/openid-connect/logout",
  redirectUri: "http://localhost:5173",
  logoutRedirect: "http://localhost:5173",
  scope: "openid email profile",
  pkce: true,
  codeChallengeMethod: "S256",
};
