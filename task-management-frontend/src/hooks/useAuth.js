import keycloak from "../keycloak";

export function useAuth() {
  const roles = keycloak.tokenParsed?.realm_access?.roles ?? [];

  return {
    username: keycloak.tokenParsed?.preferred_username,
    email: keycloak.tokenParsed?.email,
    roles,
    isAdmin: roles.includes("ADMIN"),  
    isUser: roles.includes("USER"),  
    logout: () => keycloak.logout({ redirectUri: window.location.origin }),
  };
}