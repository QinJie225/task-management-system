import { Outlet } from "react-router-dom";
import { TaskList } from "../../features/tasks/components/TaskList";
import { TASK_STATUS } from "../../utils/constants";
import "./HomePage.css";
import { FilterBar } from "../../features/tasks/components/FilterBar";
import { useTaskFilters } from "../../features/tasks/hooks/useTaskFilters";
import { usePagination } from "../../hooks/usePagination";
import { useState, useEffect } from "react";
import { taskApi } from "../../services/apiService";
import Pagination from "@mui/material/Pagination";
import Stack from "@mui/material/Stack";
import PaginationItem from "@mui/material/PaginationItem";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import ArrowForwardIcon from "@mui/icons-material/ArrowForward";

const COLUMNS = Object.keys(TASK_STATUS).map((key) => ({
  id: key,
  title: TASK_STATUS[key],
}));

export function HomePage() {
  const [tasks, setTasks] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);

  const { currentPage, pageSize, goToPage } = usePagination();

  const {
    filters,
    assignees,
    filteredTasks,
    handleFilterChange,
    handleClearFilters,
  } = useTaskFilters(tasks ?? []);

  useEffect(() => {
    fetchTasks();
  }, [currentPage]);

  const fetchTasks = async () => {
    setLoading(true);
    try {
      const data = await taskApi.getAllTasks(currentPage, pageSize);
      setTasks(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (event, value) => {
    goToPage(value - 1);
  };

  return (
    <div className="board-container">
      <FilterBar
        filters={filters}
        onChange={handleFilterChange}
        onClear={handleClearFilters}
        assignees={assignees}
      />

      <Outlet />

      {loading ? (
        <p>Loading...</p>
      ) : (
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
      )}

      <div className="pagination-bar">
        <Stack spacing={2} alignItems="center" marginTop={2}>
          <Pagination
            count={totalPages}
            page={currentPage + 1}
            onChange={handlePageChange}
            color="primary"
            renderItem={(item) => (
              <PaginationItem
                slots={{ previous: ArrowBackIcon, next: ArrowForwardIcon }}
                {...item}
              />
            )}
          />
          <p>Total tasks: {totalElements}</p>
        </Stack>
      </div>
    </div>
  );
}
