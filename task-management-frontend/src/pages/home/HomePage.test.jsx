import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { HomePage } from "./HomePage";
import { taskApi } from "../../services/apiService";

vi.mock("../../services/apiService", () => ({
  taskApi: {
    getAllTasks: vi.fn(),
  },
}));

vi.mock("../../utils/constants", () => ({
  TASK_STATUS: {
    TODO: "To Do",
    IN_PROGRESS: "In Progress",
    DONE: "Done",
  },
}));

vi.mock("../../features/tasks/components/FilterBar", () => ({
  FilterBar: () => <div data-testid="mock-filter-bar">FilterBar</div>,
}));

vi.mock("../../features/tasks/components/TaskList", () => ({
  TaskList: ({ title, tasks }) => (
    <div data-testid={`mock-task-list-${title}`}>
      {title} - Tasks: {tasks.length}
    </div>
  ),
}));

const mockTasksData = {
  content: [
    { id: 1, title: "Task 1", status: "TODO" },
    { id: 2, title: "Task 2", status: "TODO" },
    { id: 3, title: "Task 3", status: "IN_PROGRESS" },
  ],
  totalPages: 3,
  totalElements: 25,
};

function renderHomePage() {
  return render(
    <MemoryRouter>
      <HomePage />
    </MemoryRouter>
  );
}

describe("HomePage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders loading state initially", async () => {
    taskApi.getAllTasks.mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(mockTasksData), 100))
    );

    renderHomePage();

    expect(screen.getByText("Loading...")).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
    });
  });

  it("fetches and displays tasks successfully", async () => {
    taskApi.getAllTasks.mockResolvedValue(mockTasksData);

    renderHomePage();

    expect(taskApi.getAllTasks).toHaveBeenCalled();

    await waitFor(() => {
      expect(screen.getByTestId("mock-task-list-TODO")).toHaveTextContent("TODO - Tasks: 2");
    });
    expect(screen.getByTestId("mock-task-list-IN_PROGRESS")).toHaveTextContent("IN_PROGRESS - Tasks: 1");
    expect(screen.getByTestId("mock-task-list-DONE")).toHaveTextContent("DONE - Tasks: 0");
  });

  it("renders the FilterBar", async () => {
    taskApi.getAllTasks.mockResolvedValue(mockTasksData);
    renderHomePage();

    await waitFor(() => {
      expect(screen.getByTestId("mock-filter-bar")).toBeInTheDocument();
    });
  });

  it("renders pagination controls with correct totals", async () => {
    taskApi.getAllTasks.mockResolvedValue(mockTasksData);
    renderHomePage();

    await waitFor(() => {
      expect(screen.getByText("Total tasks: 25")).toBeInTheDocument();
    });

    expect(screen.getByRole("button", { name: /go to page 3/i })).toBeInTheDocument();
  });

  it("triggers API fetch when pagination page is changed", async () => {
    const user = userEvent.setup();
    taskApi.getAllTasks.mockResolvedValue(mockTasksData);

    renderHomePage();

    await waitFor(() => {
      expect(screen.getByText("Total tasks: 25")).toBeInTheDocument();
    });

    const page2Button = screen.getByRole("button", { name: /go to page 2/i });
    await user.click(page2Button);

    await waitFor(() => {
      expect(taskApi.getAllTasks).toHaveBeenCalledTimes(2);
      expect(taskApi.getAllTasks.mock.calls[1][0]).toBe(1); 
    });
  });
});