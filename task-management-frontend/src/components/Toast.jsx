import toast from "react-hot-toast";
import "./Toast.css";
import { CheckCircle, XCircle, AlertTriangle, Info } from "lucide-react";

export default function Toast({ t, type, title, message }) {
  const icons = {
    success: <CheckCircle size={20} />,
    error: <XCircle size={20} />,
    warning: <AlertTriangle size={20} />,
    info: <Info size={20} />,
  };
  return (
    <div className={`toast toast-${type}`}>
      <div className="toast-icon">
        {icons[type]}
      </div>

      <div className="toast-content">
        <div className="toast-title">{title}</div>
        <div className="toast-message">{message}</div>
      </div>

      <button className="toast-close" onClick={() => toast.dismiss(t.id)}>
        ✕
      </button>
    </div>
  );
}
