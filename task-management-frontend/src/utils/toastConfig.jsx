import toast from "react-hot-toast";
import CustomToast from "../components/CustomToast";

export const showToast = {
  success: (title, message) =>
    toast.custom((t) => (
      <CustomToast t={t} type="success" title={title} message={message} />
    )),

  error: (title, message) =>
    toast.custom((t) => (
      <CustomToast t={t} type="error" title={title} message={message} />
    )),

  warning: (title, message) =>
    toast.custom((t) => (
      <CustomToast t={t} type="warning" title={title} message={message} />
    )),
};