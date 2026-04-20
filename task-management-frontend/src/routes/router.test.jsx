import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import {
  createMemoryRouter,
  RouterProvider,
  MemoryRouter,
  Outlet,
  useRouteError,
  isRouteErrorResponse,
} from "react-router-dom";
import { AuthContext } from "react-oauth2-code-pkce";

vi.mock("../auth/auth", () => ({
  requireAuth: vi.fn(),
  requireTaskOwnership: vi.fn(),
  getAuthData: vi.fn(),
}));

vi.mock("../services/apiService", () => ({
  taskApi: {
    getTask: vi.fn(),
    getAllTasks: vi.fn(() =>
      Promise.resolve({ content: [], totalPages: 0, totalElements: 0 })
    ),
  },
}));

vi.mock("../utils/toastConfig", () => ({
  showToast: { success: vi.fn(), error: vi.fn() },
}));

import { requireAuth, requireTaskOwnership } from "../auth/auth";
import { taskApi } from "../services/apiService";
import { ForbiddenPage } from "../pages/ForbiddenPage";
import { NotFoundPage } from "../pages/NotFoundPage";
import { SomethingWentWrongPage } from "../pages/SomethingWentWrongPage";

const userAuthCtx = {
  token: "tok",
  tokenData: {
    preferred_username: "user1",
    name: "User One",
    email: "user1@test.com",
    realm_access: { roles: ["USER"] },
  },
  logOut: vi.fn(),
  logIn: vi.fn(),
  loginInProgress: false,
  error: null,
};

const noRoleAuthCtx = {
  ...userAuthCtx,
  tokenData: { ...userAuthCtx.tokenData, realm_access: { roles: [] } },
};

function TestErrorBoundary({ authCtx }) {
  const error = useRouteError();
  if (isRouteErrorResponse(error)) {
    if (error.status === 403)
      return (
        <AuthContext.Provider value={authCtx}>
          <ForbiddenPage />
        </AuthContext.Provider>
      );
    if (error.status === 404) return <NotFoundPage />;
  }
  return <SomethingWentWrongPage error={error} />;
}


function TaskDetailsStub() {
  return (
    <div data-testid="task-details-page">
      Task Details
      <Outlet />
    </div>
  );
}

function buildRouter(path, authCtx = userAuthCtx) {
  return createMemoryRouter(
    [
      {
        path: "/",
        element: (
          <AuthContext.Provider value={authCtx}>
            <div data-testid="app-layout">
              <Outlet />
            </div>
          </AuthContext.Provider>
        ),
        errorElement: <TestErrorBoundary authCtx={authCtx} />,
        children: [
          {
            index: true,
            element: <div data-testid="home-page">Home Page</div>,
            loader: () => { requireAuth(); return null; },
          },
          {
            path: "tasks/new",
            element: <div data-testid="create-task-modal">Create Task</div>,
            loader: () => { requireAuth(); return null; },
          },
          {
            path: "tasks/:taskId",
            element: <TaskDetailsStub />,
            loader: async ({ params }) => {
              requireAuth();
              return await taskApi.getTask(params.taskId);
            },
            children: [
              { index: true, element: null },
              {
                path: "delete",
                element: <div data-testid="delete-task-modal">Delete Task</div>,
                loader: requireTaskOwnership,
              },
            ],
          },
        ],
      },
      {
        path: "*",
        element: <div data-testid="not-found-page">404 Not Found</div>,
      },
    ],
    { initialEntries: [path] }
  );
}

function renderAtPath(path, authCtx = userAuthCtx) {
  return render(<RouterProvider router={buildRouter(path, authCtx)} />);
}

function MemoryRouterWrapper({ children }) {
  return <MemoryRouter>{children}</MemoryRouter>;
}

describe("Router – protected route behaviour", () => {
  beforeEach(() => {
    vi.mocked(requireAuth).mockImplementation(() => {});
    vi.mocked(requireTaskOwnership).mockResolvedValue({
      task: { taskId: "TASK-001", createdBy: "user1" },
    });
    vi.mocked(taskApi.getTask).mockResolvedValue({
      taskId: "TASK-001",
      createdBy: "user1",
      title: "T",
      description: "D",
      status: "PENDING",
      priority: "LOW",
      dueDate: "2026-06-01",
      createdAt: "2026-04-01T10:00:00",
      updatedAt: "2026-04-01T10:00:00",
    });
  });

  afterEach(() => vi.clearAllMocks());

  it("renders HomePage at '/'", async () => {
    renderAtPath("/");
    await waitFor(() =>
      expect(screen.getByTestId("home-page")).toBeInTheDocument()
    );
  });

  it("renders CreateTaskModal at '/tasks/new'", async () => {
    renderAtPath("/tasks/new");
    await waitFor(() =>
      expect(screen.getByTestId("create-task-modal")).toBeInTheDocument()
    );
  });

  it("renders TaskDetailsPage at '/tasks/:taskId'", async () => {
    renderAtPath("/tasks/TASK-001");
    await waitFor(() =>
      expect(screen.getByTestId("task-details-page")).toBeInTheDocument()
    );
  });

  it("renders DeleteTaskModal at '/tasks/:taskId/delete' when user owns task", async () => {
    renderAtPath("/tasks/TASK-001/delete");
    await waitFor(() =>
      expect(screen.getByTestId("delete-task-modal")).toBeInTheDocument()
    );
  });

  it("renders NotFoundPage for unknown routes", async () => {
    renderAtPath("/this/does/not/exist");
    await waitFor(() =>
      expect(screen.getByTestId("not-found-page")).toBeInTheDocument()
    );
  });

  it("calls requireAuth on the home route loader", async () => {
    renderAtPath("/");
    await waitFor(() => screen.getByTestId("home-page"));
    expect(requireAuth).toHaveBeenCalled();
  });

  it("calls requireAuth on the tasks/new route loader", async () => {
    renderAtPath("/tasks/new");
    await waitFor(() => screen.getByTestId("create-task-modal"));
    expect(requireAuth).toHaveBeenCalled();
  });

  it("calls requireTaskOwnership on the delete route loader", async () => {
    renderAtPath("/tasks/TASK-001/delete");
    await waitFor(() => screen.getByTestId("delete-task-modal"));
    expect(requireTaskOwnership).toHaveBeenCalled();
  });

  it("shows ForbiddenPage when requireAuth throws 403", async () => {
    vi.mocked(requireAuth).mockImplementation(() => {
      throw new Response("Forbidden", { status: 403 });
    });
    renderAtPath("/");
    await waitFor(() =>
      expect(screen.getByRole("heading", { name: "403" })).toBeInTheDocument()
    );
  });

  it("shows ForbiddenPage when requireTaskOwnership throws 403", async () => {
    vi.mocked(requireTaskOwnership).mockRejectedValue(
      new Response("Forbidden", { status: 403 })
    );
    renderAtPath("/tasks/TASK-001/delete");
    await waitFor(() =>
      expect(screen.getByRole("heading", { name: "403" })).toBeInTheDocument()
    );
  });
});

describe("ForbiddenPage – login-aware rendering", () => {
  it("shows Log Out button when user has no valid role", () => {
    render(
      <AuthContext.Provider value={noRoleAuthCtx}>
        <MemoryRouterWrapper>
          <ForbiddenPage />
        </MemoryRouterWrapper>
      </AuthContext.Provider>
    );
    expect(screen.getByRole("button", { name: "Log Out" })).toBeInTheDocument();
    expect(screen.queryByText("Go Back Home")).not.toBeInTheDocument();
  });

  it("shows Go Back Home link when user has a valid role", () => {
    render(
      <AuthContext.Provider value={userAuthCtx}>
        <MemoryRouterWrapper>
          <ForbiddenPage />
        </MemoryRouterWrapper>
      </AuthContext.Provider>
    );
    expect(screen.getByRole("link", { name: "Go Back Home" })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Log Out" })).not.toBeInTheDocument();
  });

  it("renders the 403 heading", () => {
    render(
      <AuthContext.Provider value={userAuthCtx}>
        <MemoryRouterWrapper>
          <ForbiddenPage />
        </MemoryRouterWrapper>
      </AuthContext.Provider>
    );
    expect(screen.getByRole("heading", { name: "403" })).toBeInTheDocument();
  });
});

describe("NotFoundPage – page rendering", () => {
  it("renders the 404 heading and go home link", () => {
    render(
      <MemoryRouterWrapper>
        <NotFoundPage />
      </MemoryRouterWrapper>
    );
    expect(screen.getByRole("heading", { name: "404" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Go Back Home" })).toBeInTheDocument();
  });
});

describe("SomethingWentWrongPage – page rendering", () => {
  it("renders the error message and Log Out button", () => {
    render(
      <AuthContext.Provider value={userAuthCtx}>
        <MemoryRouterWrapper>
          <SomethingWentWrongPage error={{ message: "Unexpected failure" }} />
        </MemoryRouterWrapper>
      </AuthContext.Provider>
    );
    expect(screen.getByText("Unexpected failure")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Log Out" })).toBeInTheDocument();
  });

  it("shows default message when no error message provided", () => {
    render(
      <AuthContext.Provider value={userAuthCtx}>
        <MemoryRouterWrapper>
          <SomethingWentWrongPage error={{}} />
        </MemoryRouterWrapper>
      </AuthContext.Provider>
    );
    expect(screen.getByText("An unexpected error occurred.")).toBeInTheDocument();
  });
});