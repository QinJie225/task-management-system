import toast from "react-hot-toast";
import Toast from "../components/Toast";

export const showToast = {
  success: (title, message) =>
    toast.custom((t) => (
      <Toast t={t} type="success" title={title} message={message} />
    )),

  error: (title, message) =>
    toast.custom((t) => (
      <Toast t={t} type="error" title={title} message={message} />
    )),

  warning: (title, message) =>
    toast.custom((t) => (
      <Toast t={t} type="warning" title={title} message={message} />
    )),
};