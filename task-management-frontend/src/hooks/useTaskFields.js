import { taskApi } from "../services/apiService.js";
import { useState, useEffect, useRef } from "react";
import { useFetcher } from "react-router-dom";
import { getAuthData } from "../utils/auth.js";

export function useTaskFields(task) {
  const { username } = getAuthData;
  const fetcher = useFetcher();
  const originalFields = useRef({
    title: task.title,
    description: task.description,
    priority: task.priority,
    dueDate: task.dueDate,
    status: task.status,
  });

  const [fields, setFields] = useState({
    title: task.title,
    description: task.description,
    priority: task.priority,
    dueDate: task.dueDate,
    status: task.status,
    updatedBy: task.updatedBy ?? "U",
    updatedAt: task.updatedAt,
    createdAt: task.createdAt,
    createdBy: task.createdBy ?? "U",
  });

  const [errors, setErrors] = useState({});
  const [isRefetching, setIsRefetching] = useState(false);

  const saveField = (field, value, currentUsername) => {
    setErrors({});
    fetcher.submit(
      { [field]: value, updatedBy: currentUsername },
      { method: "post", action: `/tasks/${task.taskId}/update` },
    );
  };

  const handleChange = (field, value) => {
    const currentUsername = username || "User";
    setFields((prev) => ({
      ...prev,
      [field]: value,
      updatedBy: currentUsername,
    }));
    saveField(field, value, currentUsername);
  };

  const revertField = (field) => {
    setFields((prev) => ({ ...prev, [field]: originalFields.current[field] }));
    setErrors((prev) => ({ ...prev, [field]: null }));
  };

  useEffect(() => {
    if (fetcher.data?.errors) {
      setErrors(fetcher.data.errors);
    }
    if (fetcher.data?.success) {
      setErrors({});
      originalFields.current = {
        title: fields.title,
        description: fields.description,
        priority: fields.priority,
        dueDate: fields.dueDate,
        status: fields.status,
      };

      setIsRefetching(true);

      setTimeout(async () => {
        const updatedTask = await taskApi.getTask(task.taskId);
        setFields((prev) => ({
          ...prev,
          updatedAt: updatedTask.updatedAt,
          updatedBy: updatedTask.updatedBy ?? "U",
        }));
        setIsRefetching(false);
      }, 1000);
    }
  }, [fetcher.data]);

  return {
    fields,
    setFields,
    handleChange,
    revertField,
    fetcher,
    errors,
    isRefetching,
  };
}
