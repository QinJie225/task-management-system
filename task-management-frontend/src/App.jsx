import { RouterProvider } from "react-router-dom";
import { Toaster } from "react-hot-toast";
import { createAppRouter } from "./routes/router.jsx";
import { useMemo, useContext } from "react";
import { AuthContext } from "react-oauth2-code-pkce";
import { useAuthReady } from "./hooks/useAuthReady";

export default function App() {
  const authStatus = useAuthReady();
  const { logIn } = useContext(AuthContext);

  const router = useMemo(() => {
    if (authStatus !== "ready") return null;
    return createAppRouter();
  }, [authStatus]);

  if (authStatus === "loading") {
    return <div>Loading...</div>;
  }

  if (authStatus === "unauthenticated") {
    logIn();
    return <div>Redirecting to login...</div>;
  }

  return (
    <>
      <Toaster position="top-right" />
      <RouterProvider router={router} />
    </>
  );
}