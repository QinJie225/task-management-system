import { RouterProvider } from "react-router-dom";
import { Toaster } from "react-hot-toast";
import { createAppRouter } from "./routes/router.jsx";
import { useMemo } from "react";
import { useAuthReady } from "./hooks/useAuthReady";

export default function App() {
  const isAuthReady = useAuthReady();

  const router = useMemo(() => {
    console.log("kakakakakkakak");
    console.log(isAuthReady);
    if (!isAuthReady) return null;
    return createAppRouter();
  }, [isAuthReady]);

  if (!router) {
    console.log("ninininini");
    return <div>Loading...</div>;
  }
  return (
    <>
      <Toaster position="top-right" />
      <RouterProvider router={router} />
    </>
  );
}
