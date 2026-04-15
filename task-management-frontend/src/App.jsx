import { RouterProvider } from "react-router-dom";
import { Toaster } from 'react-hot-toast';

export default function App({ router }) {
  return (
    <>
      <Toaster position="top-right" reverseOrder={false} />
      <RouterProvider router={router} />
    </>
  );
}