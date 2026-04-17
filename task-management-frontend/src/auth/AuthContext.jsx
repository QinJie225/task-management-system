// import React, { createContext, useContext, useEffect, useState, useRef } from "react";
// import keycloak from "../keycloak.js";

// const AuthContext = createContext(null);

// export const AuthProvider = ({ children }) => {
//   const [initialized, setInitialized] = useState(false);
//   const [authenticated, setAuthenticated] = useState(false);
//   const [token, setToken] = useState(null);
  
//   const isInitCalled = useRef(false);

//   useEffect(() => {
//     if (isInitCalled.current) return;
//     isInitCalled.current = true;

//     keycloak
//       .init({
//         onLoad: "login-required",
//         checkLoginIframe: false,
//         pkceMethod: "S256",
//       })
//       .then((auth) => {
//         setAuthenticated(auth);
//         setToken(keycloak.token);
//         setInitialized(true);
//         keycloak.onTokenExpired = () => {
//           keycloak.updateToken(30).then((refreshed) => {
//             if (refreshed) setToken(keycloak.token);
//           }).catch(() => keycloak.login());
//         };
//       })
//       .catch((err) => {
//         console.error("Keycloak init failed", err);
//         setInitialized(true);
//       });
//   }, []);

//   const value = {
//     keycloak,
//     initialized,
//     authenticated,
//     token,
//     user: keycloak.tokenParsed,
//     logout: () => keycloak.logout(),
//   };

//   if (!initialized) {
//     return <div className="loading-screen">Authenticating...</div>;
//   }

//   return (
//     <AuthContext.Provider value={value}>
//       {children}
//     </AuthContext.Provider>
//   );
// };

// export const useAuth = () => useContext(AuthContext);