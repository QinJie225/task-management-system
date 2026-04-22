import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { useLoaderData, useFetcher } from "react-router-dom";
import { AuthContext } from "react-oauth2-code-pkce";
import { TaskDetailsPage } from "../../pages/tasks/TaskDetailsPage";

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useLoaderData: vi.fn(),
    useFetcher: vi.fn(() => ({
      data: null,
      state: "idle",
      submit: vi.fn(),
      Form: ({ children }) => <form>{children}</form>,
    })),
    Outlet: () => null,
    Link: ({ to, children, className }) => (
      <a href={to} className={className}>
        {children}
      </a>
    ),
  };
});

const mockTask = {
  taskId: "TASK-001",
  title: "Fix login bug",
  description: "Users cannot log in with SSO",
  status: "IN_PROGRESS",
  priority: "HIGH",
  dueDate: "2026-06-01",
  createdAt: "2026-04-01T10:00:00",
  updatedAt: "2026-04-10T12:00:00",
  createdBy: "user1",
  updatedBy: "user1",
};

const defaultFetcher = {
  data: null,
  state: "idle",
  submit: vi.fn(),
  Form: ({ children }) => <form>{children}</form>,
};

function renderWithAuth(authValue) {
  vi.mocked(useLoaderData).mockReturnValue(mockTask);
  return render(
    <AuthContext.Provider value={authValue}>
      <MemoryRouter>
        <TaskDetailsPage />
      </MemoryRouter>
    </AuthContext.Provider>,
  );
}

const userAuth = {
  token: "tok",
  tokenData: {
    preferred_username: "user1",
    name: "User One",
    email: "user1@test.com",
    realm_access: { roles: ["USER"] },
  },
  logOut: vi.fn(),
};

const adminAuth = {
  token: "tok",
  tokenData: {
    preferred_username: "admin1",
    name: "Admin One",
    email: "admin@test.com",
    realm_access: { roles: ["ADMIN"] },
  },
  logOut: vi.fn(),
};

const otherUserAuth = {
  token: "tok",
  tokenData: {
    preferred_username: "bob",
    name: "Bob",
    email: "bob@test.com",
    realm_access: { roles: ["USER"] },
  },
  logOut: vi.fn(),
};

describe("TaskDetailsPage – page rendering", () => {
  it("renders task title", () => {
    renderWithAuth(userAuth);
    expect(
      screen.getByRole("heading", { name: "Fix login bug", level: 1 }),
    ).toBeInTheDocument();
  });

  it("renders task description", () => {
    renderWithAuth(userAuth);
    expect(
      screen.getByText("Users cannot log in with SSO"),
    ).toBeInTheDocument();
  });

  it("renders createdBy (Reporter) in sidebar", () => {
    renderWithAuth(userAuth);
    expect(screen.getAllByText("user1").length).toBeGreaterThan(0);
  });

  it("renders breadcrumb back link", () => {
    renderWithAuth(userAuth);
    expect(screen.getByText("Task Dashboard")).toBeInTheDocument();
    expect(
      screen.getByText(mockTask.title, { selector: ".breadcrumb-title" }),
    ).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "" })).toHaveAttribute("href", "/");
  });

  it("renders sidebar details with correct data population", () => {
    renderWithAuth(userAuth);

    expect(screen.getByText("Status")).toBeInTheDocument();
    expect(screen.getByText("Priority")).toBeInTheDocument();
    expect(screen.getByText("Due date")).toBeInTheDocument();

    expect(screen.getByText("In Progress")).toBeInTheDocument();
    expect(screen.getByText("High")).toBeInTheDocument();
    expect(screen.getByDisplayValue("2026-06-01")).toBeInTheDocument();
  });
});

describe("TaskDetailsPage – role-based UI (owner USER)", () => {
  it("shows Delete button for task owner", () => {
    renderWithAuth(userAuth);
    expect(screen.getByRole("link", { name: "Delete" })).toBeInTheDocument();
  });

  it("shows edit pencil icon for task owner", () => {
    renderWithAuth(userAuth);
    expect(screen.getByRole("button", { name: "Edit" })).toBeInTheDocument();
  });
});

describe("TaskDetailsPage – role-based UI (non-owner USER)", () => {
  it("hides Delete button for tasks owned by another user", () => {
    renderWithAuth(otherUserAuth);
    expect(
      screen.queryByRole("link", { name: "Delete" }),
    ).not.toBeInTheDocument();
  });

  it("hides edit button for tasks owned by another user", () => {
    renderWithAuth(otherUserAuth);
    expect(
      screen.queryByRole("button", { name: "Edit" }),
    ).not.toBeInTheDocument();
  });
});

describe("TaskDetailsPage – role-based UI (ADMIN)", () => {
  it("shows Delete button for ADMIN on any task", () => {
    renderWithAuth(adminAuth);
    expect(screen.getByRole("link", { name: "Delete" })).toBeInTheDocument();
  });

  it("shows edit button for ADMIN on any task", () => {
    renderWithAuth(adminAuth);
    expect(screen.getByRole("button", { name: "Edit" })).toBeInTheDocument();
  });
});

describe("TaskDetailsPage – save state indicators", () => {
  it("shows Saving... badge while fetcher is submitting", () => {
    vi.mocked(useFetcher).mockReturnValue({
      data: null,
      state: "submitting",
      submit: vi.fn(),
      Form: ({ children }) => <form>{children}</form>,
    });
    renderWithAuth(userAuth);
    expect(screen.getByText("Saving...")).toBeInTheDocument();
  });

  it("shows Saved badge when fetcher returns success", () => {
    vi.mocked(useFetcher).mockReturnValue({
      data: { success: true },
      state: "idle",
      submit: vi.fn(),
      Form: ({ children }) => <form>{children}</form>,
    });
    renderWithAuth(userAuth);
    expect(screen.getByText("Saved")).toBeInTheDocument();
  });
});