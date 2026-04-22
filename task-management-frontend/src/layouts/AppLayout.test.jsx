// src/layouts/AppLayout.test.jsx
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { AppLayout } from "./AppLayout";
import { useAuthData } from "../hooks/useAuthData";

vi.mock("../hooks/useAuthData", () => ({
  useAuthData: vi.fn(),
}));

const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    Outlet: () => <div data-testid="mock-outlet">Main Content Area</div>,
  };
});

vi.mock("../utils/formatAvatarIcon", () => ({
  formatAvatarIcon: (username) => (
    <span data-testid="avatar">{username?.charAt(0).toUpperCase()}</span>
  ),
}));

function renderLayout() {
  return render(
    <MemoryRouter>
      <AppLayout />
    </MemoryRouter>,
  );
}

describe("AppLayout Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the dashboard shell and main content area", () => {
    useAuthData.mockReturnValue({
      name: "Test User",
      username: "testuser",
      email: "test@test.com",
      isAdmin: false,
      logout: vi.fn(),
    });

    renderLayout();

    expect(screen.getByText("Task Dashboard")).toBeInTheDocument();
    expect(screen.getByTestId("mock-outlet")).toBeInTheDocument();
  });

  it("displays the correctly formatted current date", () => {
    const mockDate = new Date(2026, 3, 20);
    vi.setSystemTime(mockDate);

    useAuthData.mockReturnValue({ name: "User", username: "user" });
    renderLayout();

    expect(screen.getByText("Monday, 20 April 2026")).toBeInTheDocument();

    vi.useRealTimers();
  });

  it("shows 'Administrator' badge for admin users", async () => {
    const user = userEvent.setup();

    useAuthData.mockReturnValue({
      name: "Admin Alice",
      username: "admin",
      email: "admin@task.com",
      isAdmin: true,
      logout: vi.fn(),
    });

    renderLayout();

    await user.click(screen.getByText("Admin Alice"));

    expect(screen.getByText("Administrator")).toBeInTheDocument();
  });

  it("shows 'User' badge for normal users", async () => {
    const user = userEvent.setup();

    useAuthData.mockReturnValue({
      name: "User Bob",
      username: "bob",
      email: "bob@task.com",
      isAdmin: false,
      logout: vi.fn(),
    });

    renderLayout();

    await user.click(screen.getByText("User Bob"));

    expect(screen.queryByText("Administrator")).not.toBeInTheDocument();
    expect(screen.getByText("User")).toBeInTheDocument();
  });

  it("navigates to 'tasks/new' when clicking Add button", async () => {
    const user = userEvent.setup();

    useAuthData.mockReturnValue({
      name: "John",
      username: "john",
      email: "john@test.com",
      isAdmin: false,
      logout: vi.fn(),
    });

    renderLayout();

    const addBtn = screen.getByRole("button", { name: "Add New Task" });
    await user.click(addBtn);

    expect(mockNavigate).toHaveBeenCalledWith("tasks/new");
  });

  it("handles logout and fallback values correctly", async () => {
    const user = userEvent.setup();
    const mockLogout = vi.fn();

    useAuthData.mockReturnValue({
      name: null,
      username: "guest_pro",
      email: null,
      isAdmin: false,
      logout: mockLogout,
    });

    renderLayout();

    const profileTrigger = screen.getByText("guest_pro");
    expect(profileTrigger).toBeInTheDocument();

    await user.click(profileTrigger);

    const logoutBtn = screen.getByRole("button", { name: "Sign Out" });
    await user.click(logoutBtn);

    expect(mockLogout).toHaveBeenCalledTimes(1);
  });

  it("closes the user menu when clicking the avatar a second time", async () => {
    const user = userEvent.setup();
    useAuthData.mockReturnValue({
      name: "User Bob",
      username: "bob",
      logout: vi.fn(),
    });

    renderLayout();

    const profileTrigger = screen.getByText("User Bob");

    // Open
    await user.click(profileTrigger);
    expect(screen.getByText("Sign Out")).toBeInTheDocument();

    // Close
    await user.click(profileTrigger);
    expect(screen.queryByText("Sign Out")).not.toBeInTheDocument();
  });

  it("renders 'User' and 'No email' when auth data is missing", async () => {
    const user = userEvent.setup();
    useAuthData.mockReturnValue({
      name: null,
      username: null,
      email: null,
      isAdmin: false,
      logout: vi.fn(),
    });

    renderLayout();

    const trigger = screen.getByText("User", {
      selector: ".user-avatar-wrapper",
    });
    await user.click(trigger);

    expect(screen.getByText("No email")).toBeInTheDocument();
    expect(
      screen.getByText("User", { selector: ".user-name" }),
    ).toBeInTheDocument();
    expect(
      screen.getByText("User", { selector: ".user-role-badge" }),
    ).toBeInTheDocument();
  });
});