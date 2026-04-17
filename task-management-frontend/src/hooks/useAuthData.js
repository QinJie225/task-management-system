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

// // src/auth/AuthContext.jsx
// import { useAuth } from "../auth/AuthContext"; // assuming your context is here

// export const useAuthData = () => {
//   const { user, token, keycloak, isAuthenticated } = useAuth();

//   const roles = user?.realm_access?.roles ?? [];
//   const isAdmin = roles.includes("ADMIN");
//   const isUser = roles.includes("USER");
//   const username = user?.preferred_username;

//   return {
//     name: user?.name || username || "User",
//     username,
//     email: user?.email || "No email",
//     roles,
//     isAdmin,
//     isUser,
//     isAuthenticated,
//     isAuthorized: isAdmin || isUser,
//     canModify: (task) => isAdmin || task?.createdBy === username,
//     logout: () => keycloak.logout({ redirectUri: window.location.origin }),
//   };
// };