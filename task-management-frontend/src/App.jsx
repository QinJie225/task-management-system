import { RouterProvider } from "react-router-dom";
import { Toaster } from 'react-hot-toast';
import { createAppRouter } from "./routes/router.jsx";
import { useMemo } from "react";

export default function App() {
  const router = useMemo(() => createAppRouter(), []);
  return (
    <>
      <Toaster position="top-right" />
      <RouterProvider router={router} />
    </>
  );
}