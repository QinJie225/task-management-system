import { createBrowserRouter } from "react-router-dom";
import { HomePage } from "../pages/home/HomePage";
import { TaskDetailsPage } from "../pages/tasks/TaskDetailsPage";
import { taskApi } from "../services/apiService";
import {
  CreateTaskModal,
  createTaskAction,
} from "../features/modal/CreateTaskModal";
import {
  DeleteTaskModal,
  deleteTaskAction,
} from "../features/modal/DeleteTaskModal";
import { updateTaskAction } from "../pages/tasks/TaskDetailsPage";
import { NotFoundPage } from "../pages/NotFoundPage";
import { AppLayout } from "../layouts/AppLayout";
import { requireTaskOwnership, requireAuth } from "../auth/auth";
import ErrorPage from "../pages/ErrorPage";

export function createAppRouter() {
  return createBrowserRouter([
    {
      path: "/",
      element: <AppLayout />,
      errorElement: <ErrorPage />,
      children: [
        {
          index: true,
          element: <HomePage />,
          loader: async () => {
            requireAuth();
            return await taskApi.getAllTasks();
          },
        },
        {
          path: "tasks/new",
          element: <CreateTaskModal />,
          loader: () => {
            requireAuth();
            return null;
          },
          action: createTaskAction,
        },
        {
          path: "/tasks/:taskId",
          element: <TaskDetailsPage />,
          loader: async ({ params }) => {
            requireAuth();
            return await taskApi.getTask(params.taskId);
          },
          children: [
            { index: true, element: null },
            {
              path: "delete",
              element: <DeleteTaskModal />,
              loader: requireTaskOwnership,
              action: deleteTaskAction,
            },
            {
              path: "update",
              loader: requireTaskOwnership,
              action: updateTaskAction,
            },
          ],
        },
      ],
    },
    {
      path: "*",
      element: <NotFoundPage />,
    },
  ]);
}
