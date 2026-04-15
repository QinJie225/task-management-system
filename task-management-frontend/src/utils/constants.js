import { Circle, Loader2, CheckCircle2 } from "lucide-react";

export const TASK_PRIORITY = {
  LOW: {
    label: "Low",
    color: "#16a34a",
    bg: "#ffffff",
    border: "#22cc5e"
  },
  MEDIUM: {
    label: "Medium",
    color: "#5a67d8",
    bg: "#ffffff",
    border: "#094ed7",
  },
  HIGH: {
    label: "High",
    color: "#dc2626",
    bg: "#ffffff",
    border: "#ef4444",
  },
};

export const TASK_STATUS = {
  PENDING: {
    label: "Pending",
    color: "#64748b",
    bg: "#dfe2e6",
    border: "#e2e8f0",
    Icon: Circle,
  },
  IN_PROGRESS: {
    label: "In Progress",
    color: "#d97706",
    bg: "#fffbeb",
    border: "#fde68a",
    Icon: Loader2,
  },
  COMPLETED: {
    label: "Completed",
    color: "#16a34a",
    bg: "#f0fdf4",
    border: "#bbf7d0",
    Icon: CheckCircle2,
  },
};
