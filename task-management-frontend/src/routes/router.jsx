// src/router.jsx
import { createBrowserRouter } from "react-router-dom";
import { HomePage } from "../pages/home/HomePage";
import { TaskDetailsPage } from "../pages/tasks/TaskDetailsPage";
import { taskApi } from "../services/apiService";
import {
  CreateTaskModal,
  createTaskAction,
} from "../components/CreateTaskModal";
import {
  DeleteTaskModal,
  deleteTaskAction,
} from "../components/DeleteTaskModal";
import { updateTaskAction } from "../pages/tasks/TaskDetailsPage";
import { NotFoundPage } from "../pages/NotFoundPage";

export function createAppRouter() {
  return createBrowserRouter([
    {
      path: "/",
      element: <HomePage />,
      loader: async () => await taskApi.getAllTasks(),
      children: [
        { index: true, element: null },
        {
          path: "tasks/new",
          element: <CreateTaskModal />,
          action: createTaskAction,
        },
      ],
    },
    {
      path: "/tasks/:taskId",
      element: <TaskDetailsPage />,
      loader: async ({ params }) => await taskApi.getTask(params.taskId),
      children: [
        { index: true, element: null },
        {
          path: "delete",
          element: <DeleteTaskModal />,
          action: deleteTaskAction,
        },
        { path: "update", action: updateTaskAction },
      ],
    },
    {
      path: "*",
      element: <NotFoundPage />,
    },
  ]);
}
