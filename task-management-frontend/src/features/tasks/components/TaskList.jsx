import { TaskCard } from "./TaskCard";
import { TASK_STATUS } from "../../../utils/constants";
import "./TaskList.css";

export function TaskList({ title, tasks }) {
  const status = TASK_STATUS[title];
  const { Icon } = status;

  return (
    <div className="task-list-column">
      <div
        className="list-header"
        style={{ borderBottom: `2px solid ${status.color}` }}
      >
        <div className="header-left">
          <div
            className="status-pill-header"
            style={{ backgroundColor: status.bg }}
          >
            <Icon size={14} color={status.color} strokeWidth={3} />
            <h3 className="list-status" style={{ color: status.color }}>
              {status.label}
            </h3>
          </div>
          <span className="task-count-badge">{tasks.length}</span>
        </div>
      </div>

      <div className="cards-container">
        {tasks.map((task) => (
          <TaskCard key={task.id} task={task} />
        ))}
      </div>
    </div>
  );
}
