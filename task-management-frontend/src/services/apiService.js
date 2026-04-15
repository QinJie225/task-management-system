import axiosInstance from "./axiosInstance";

export const taskApi = {
  getAllTasks: async (status = null) => {
    const params = status ? { status } : {};
    const { data } = await axiosInstance.get("/tasks", { params });
    return data;
  },
  getTask: async (taskId) => {
    const { data } = await axiosInstance.get(`/tasks/${taskId}`);
    return data;
  },
  createTask: async (createTaskRequest) => {
    await axiosInstance.post("/tasks", createTaskRequest);
  },
  updateTask: async (taskId, updateTaskRequest) => {
    const { data } = await axiosInstance.patch(
      `/tasks/${taskId}`,
      updateTaskRequest,
    );
    return data;
  },
  deleteTask: async (taskId) => {
    await axiosInstance.delete(`/tasks/${taskId}`);
  },
};
