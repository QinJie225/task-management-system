import { useContext } from "react";
import { AuthContext } from "react-oauth2-code-pkce";

export function useAuthReady() {
  const { token, loginInProgress } = useContext(AuthContext);
  if (loginInProgress) return "loading";
  if (token) return "ready";
  return "unauthenticated";
}