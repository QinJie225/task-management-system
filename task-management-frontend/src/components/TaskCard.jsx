import "./TaskCard.css";
import { Calendar, Flag } from "lucide-react";
import { TASK_PRIORITY } from "../utils/constants";
import { formatDate } from "../utils/formatDate";
import { Link } from "react-router-dom";

function PriorityBadge({ priority }) {
  const taskPriority = TASK_PRIORITY[priority];

  return (
    <div className="badge-pill" style={{
        background: taskPriority.bg,
        border: `1px solid ${taskPriority.border}`,
      }}>
      <Flag size={12} color={taskPriority.color} strokeWidth={3} />
      <span className="badge-text" style={{ color: taskPriority.color }}>{taskPriority.label}</span>
    </div>
  );
}

export function TaskCard({ task }) {
  const initial = task.createdBy ? task.createdBy[0].toUpperCase() : "U";

  return (
    <Link to={`/tasks/${task.taskId}`} className="task-card-link">
        <div className="task-card">
      <div className="task-card-header">
        <h4 className="task-card-title">{task.title}</h4>
        <PriorityBadge priority={task.priority} />
      </div>

      <div className="task-card-footer">
        <div className="meta-row">
          <Calendar size={14} className="meta-icon" />
          <span className="meta-text">{formatDate(task.createdAt)}</span>
        </div>

        <div className="user-meta">
          <div className="mini-avatar">{initial}</div>
          <span className="meta-text">{task.createdBy}</span>
        </div>
      </div>
    </div>
    </Link>
  );
}
