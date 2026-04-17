import { useContext } from "react";
import { AuthContext } from "react-oauth2-code-pkce";

export function useAuthReady() {
  const { token, loginInProgress } = useContext(AuthContext);
  return !loginInProgress;
}