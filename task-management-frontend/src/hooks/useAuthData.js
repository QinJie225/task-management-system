import { useContext, useMemo } from "react";
import { AuthContext } from "react-oauth2-code-pkce";

export const useAuthData = () => {
  const { token, tokenData, logOut, error } = useContext(AuthContext);

  const roles = useMemo(
    () => tokenData?.realm_access?.roles ?? [],
    [tokenData]
  );

  const username = tokenData?.preferred_username;
  const isAdmin = roles.includes("ADMIN");
  const isUser = roles.includes("USER");

  return {
    token,
    name: tokenData?.name || username || "User",
    username,
    email: tokenData?.email,
    roles,
    isAdmin,
    isUser,
    isAuthenticated: !!token,
    isAuthorized: isAdmin || isUser,
    error,
    canModify: (task) => isAdmin || task?.createdBy === username,
    logout: logOut,
  };
};