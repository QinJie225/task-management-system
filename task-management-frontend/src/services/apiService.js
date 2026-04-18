import axiosInstance from "./axiosInstance";

export const taskApi = {
  getAllTasks: async (page=0, size=10, sortBy="createdAt", direction="desc") => {
    const { data } = await axiosInstance.get("/tasks", { params: {page, size, sortBy, direction} });
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
