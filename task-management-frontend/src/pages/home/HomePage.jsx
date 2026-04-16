import { useLoaderData, Outlet } from "react-router-dom";
import { TaskList } from "../../components/TaskList";
import { TASK_STATUS } from "../../utils/constants";
import "./HomePage.css";
import { FilterBar } from "../../components/FilterBar";
import { useTaskFilters } from "../../hooks/useTaskFilters";

const COLUMNS = Object.keys(TASK_STATUS).map((key) => ({
  id: key,
  title: TASK_STATUS[key],
}));

export function HomePage() {
  const tasks = useLoaderData();
  const {
    filters,
    assignees,
    filteredTasks,
    handleFilterChange,
    handleClearFilters,
  } = useTaskFilters(tasks);

  return (
    <div className="board-container">
      <FilterBar
        filters={filters}
        onChange={handleFilterChange}
        onClear={handleClearFilters}
        assignees={assignees}
      />

      <Outlet />

      <div className="kanban-board">
        {COLUMNS.map((column) => {
          const columnTasks = filteredTasks.filter(
            (task) => task.status === column.id,
          );
          return (
            <TaskList key={column.id} title={column.id} tasks={columnTasks} />
          );
        })}
      </div>
    </div>
  );
}
