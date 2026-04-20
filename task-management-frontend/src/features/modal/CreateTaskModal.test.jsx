// features/CreateTaskModal.test.jsx
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import {
  CreateTaskModal,
  createTaskAction,
} from "../../features/modal/CreateTaskModal";
import { taskApi } from "../../services/apiService";

let mockActionData = null;

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useActionData: () => mockActionData,
    useNavigate: vi.fn(() => vi.fn()),
    redirect: vi.fn((to) => ({ type: "redirect", location: to })),
  };
});

vi.mock("../../services/apiService", () => ({
  taskApi: { createTask: vi.fn() },
}));

vi.mock("../../utils/toastConfig", () => ({
  showToast: { success: vi.fn(), error: vi.fn() },
}));

beforeEach(() => {
  mockActionData = null;
  vi.clearAllMocks();
});

function renderModal() {
  const router = createMemoryRouter(
    [
      {
        path: "/tasks/new",
        element: <CreateTaskModal />,
        action: createTaskAction,
      },
    ],
    { initialEntries: ["/tasks/new"] },
  );
  return render(<RouterProvider router={router} />);
}

function makeRequest(data) {
  const formData = new FormData();
  Object.entries(data).forEach(([k, v]) => formData.append(k, v));
  return { formData: async () => formData };
}

describe("CreateTaskModal – rendering", () => {
  it("renders all form fields", () => {
    renderModal();

    expect(
      screen.getByPlaceholderText("What needs to be done?"),
    ).toBeInTheDocument();
    const select = screen.getByRole("combobox");
    expect(select).toBeInTheDocument();
    expect(
      screen.getByRole("option", { name: "Select priority" }),
    ).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "Low" })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "Medium" })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "High" })).toBeInTheDocument();
    expect(document.querySelector("input[name='dueDate']")).toBeInTheDocument();
    expect(
      screen.getByPlaceholderText("Add more details..."),
    ).toBeInTheDocument();
  });

  it("renders submit and cancel buttons", () => {
    renderModal();

    expect(
      screen.getByRole("button", { name: "Create Task" }),
    ).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Cancel" })).toBeInTheDocument();
  });
});

describe("CreateTaskModal – field error rendering", () => {
  it("shows title error", () => {
    mockActionData = { errors: { title: ["Title is required"] } };
    renderModal();
    expect(screen.getByText("Title is required")).toBeInTheDocument();
  });

  it("shows multiple errors", () => {
    mockActionData = {
      errors: {
        title: ["Title is required"],
        priority: ["Priority is required"],
      },
    };
    renderModal();

    expect(screen.getByText("Title is required")).toBeInTheDocument();
    expect(screen.getByText("Priority is required")).toBeInTheDocument();
  });
});

describe("createTaskAction – validation (Zod schema)", () => {
  const validData = {
    title: "Valid title",
    description: "This is a valid description",
    priority: "HIGH",
    dueDate: "2099-12-31",
  };

  it("fails when title is too short", async () => {
    const result = await createTaskAction({
      request: makeRequest({ ...validData, title: "Hi" }),
    });

    expect(result.errors.title[0]).toMatch(
      "Title must be at least 3 characters",
    );
  });

  it("fails when title is too long", async () => {
    const result = await createTaskAction({
      request: makeRequest({ ...validData, title: "a".repeat(36) }),
    });

    expect(result.errors.title[0]).toMatch(
      "Title must be at most 35 characters",
    );
  });

  it("fails when description is too short", async () => {
    const result = await createTaskAction({
      request: makeRequest({ ...validData, description: "short" }),
    });

    expect(result.errors.description[0]).toMatch(
      "Description must be at least 10 characters",
    );
  });

  it("fails when description is too long", async () => {
    const result = await createTaskAction({
      request: makeRequest({ ...validData, description: "a".repeat(501) }),
    });

    expect(result.errors.description[0]).toMatch(
      "Description must be at most 500 characters",
    );
  });

  it("fails when priority is invalid", async () => {
    const result = await createTaskAction({
      request: makeRequest({ ...validData, priority: "URGENT" }),
    });

    expect(result.errors.priority).toBeDefined();
  });

  it("fails when due date is empty", async () => {
    const result = await createTaskAction({
      request: makeRequest({ ...validData, dueDate: "" }),
    });

    expect(result.errors.dueDate[0]).toMatch("Due date is required");
  });

  it("fails when due date is in the past", async () => {
    const result = await createTaskAction({
      request: makeRequest({ ...validData, dueDate: "2020-01-01" }),
    });

    expect(result.errors.dueDate[0]).toMatch("Due date must be today or in the future");
  });

  it("fails when due date format is invalid", async () => {
    const result = await createTaskAction({
      request: makeRequest({ ...validData, dueDate: "not-a-date" }),
    });

    expect(result.errors.dueDate).toBeDefined();
  });

  it("passes when due date is today", async () => {
    const today = new Date().toISOString().split("T")[0];

    taskApi.createTask.mockResolvedValue(undefined);

    await createTaskAction({
      request: makeRequest({ ...validData, dueDate: today }),
    });

    expect(taskApi.createTask).toHaveBeenCalled();
  });

  it("calls API when all fields are valid", async () => {
    taskApi.createTask.mockResolvedValue(undefined);

    await createTaskAction({
      request: makeRequest(validData),
    });

    expect(taskApi.createTask).toHaveBeenCalledTimes(1);
  });

  it("returns error when API fails", async () => {
    taskApi.createTask.mockRejectedValue(new Error("Network error"));

    const result = await createTaskAction({
      request: makeRequest(validData),
    });

    expect(result.error).toBeDefined();
  });

  it("returns errors for empty form", async () => {
    const result = await createTaskAction({
      request: makeRequest({}),
    });

    expect(result.errors).toBeDefined();
    expect(result.errors.title).toBeDefined();
  });
});