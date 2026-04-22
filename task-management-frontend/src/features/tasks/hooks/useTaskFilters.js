import { useState, useMemo } from "react";

export function useTaskFilters(tasks) {
  const [filters, setFilters] = useState({ priority: "", createdBy: "" });

  const assignees = useMemo(() => {
    return [...new Set(tasks.map((t) => t.createdBy).filter(Boolean))];
  }, [tasks]);

  const filteredTasks = useMemo(() => {
    return tasks.filter((task) => {
      const matchedPriority = !filters.priority || task.priority === filters.priority;
      const matchedCreator = !filters.createdBy || task.createdBy === filters.createdBy;
      return matchedPriority && matchedCreator;
    });
  }, [tasks, filters]);

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({...prev, [key]: value}));
  }

  const handleClearFilters = () => {
    setFilters({priority: "", createdBy: ""});
  }

  return {
    filters,
    assignees,
    filteredTasks,
    handleFilterChange,
    handleClearFilters
  }
}
