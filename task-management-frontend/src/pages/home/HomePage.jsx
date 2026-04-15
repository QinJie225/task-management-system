import { useState} from "react";
import { useLoaderData } from "react-router-dom";
import { useNavigate, Outlet } from "react-router-dom";
import { TaskList } from "../../components/TaskList";
import dayjs from "dayjs";
import { TASK_STATUS } from "../../utils/constants";
import { formatAvatarIcon } from "../../utils/formatAvatarIcon";
import "./HomePage.css";
import { LogOut, Plus, ChevronDown } from "lucide-react";
import keycloak from "../../keycloak";
import { FilterBar } from "../../components/FilterBar";

const COLUMNS = Object.keys(TASK_STATUS).map((key) => ({
  id: key,
  title: TASK_STATUS[key],
}));

export function HomePage() {
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const tasks = useLoaderData();
  const navigate = useNavigate();

  const openUserMenu = () => {
    setIsUserMenuOpen(!isUserMenuOpen);
  };

  const userInfo = {
    name:
      keycloak.tokenParsed?.preferred_username ||
      keycloak.tokenParsed?.given_name ||
      "User",
    email: keycloak.tokenParsed?.email || "No email",
    role: keycloak.hasRealmRole("ADMIN") ? "Administrator" : "User",
  };

  const handleLogout = () => {
    keycloak.logout({
      redirectUri: window.location.origin,
    });
  };

  //
  const [filters, setFilters] = useState({ priority: "", createdBy: "" });
  const assignees = [...new Set(tasks.map((t) => t.createdBy).filter(Boolean))];
  const filteredTasks = tasks.filter((task) => {
    if (filters.priority && task.priority !== filters.priority) return false;
    if (filters.createdBy && task.createdBy !== filters.createdBy) return false;
    return true;
  });

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
  };

  const handleClearFilters = () => {
    setFilters({ priority: "", assignee: "" });
  };

  //
  return (
    <div className="app-container">
      <main className="main-content">
        <div className="board-container">
          <header className="project-header">
            <div className="header-top">
              <div className="title-section">
                <h1 className="project-title">Task Dashboard</h1>
                <p className="dashboard-current-date">
                  {dayjs().format("dddd, D MMMM YYYY")}
                </p>
              </div>

              <div className="header-actions">
                <button
                  className="add-task-btn"
                  onClick={() => navigate("tasks/new")}
                >
                  <Plus size={14} /> Add New Task
                </button>
                <div
                  className="nav-icon-wrapper user-avatar-wrapper"
                  onClick={openUserMenu}
                >
                  <div className="mini-avatar">
                    {formatAvatarIcon(userInfo.name)}
                  </div>
                  {userInfo.name}
                  <ChevronDown size={11} />
                </div>

                {isUserMenuOpen && (
                  <div className="user-dropdown">
                    <div className="user-details">
                      <span className="user-name">{userInfo.name}</span>
                      <span className="user-email">{userInfo.email}</span>
                      <span className="user-role-badge">{userInfo.role}</span>
                    </div>

                    <hr className="dropdown-divider" />
                    <button
                      className="dropdown-item logout"
                      onClick={handleLogout}
                    >
                      <LogOut size={16} /> Sign Out
                    </button>
                  </div>
                )}
              </div>
            </div>
          </header>

          {}
          <FilterBar
            filters={filters}
            onChange={handleFilterChange}
            onClear={handleClearFilters}
            assignees={assignees}
          />

          {}

          <Outlet />

          {/* <div className="kanban-board">
            {COLUMNS.map((column) => {
              const columnTasks = tasks.filter(
                (task) => task.status === column.id,
              );

              return (
                <TaskList
                  key={column.id}
                  title={column.id}
                  tasks={columnTasks}
                />
              );
            })}
          </div> */}

          <div className="kanban-board">
            {COLUMNS.map((column) => {
              const columnTasks = filteredTasks.filter(
                (task) => task.status === column.id,
              );
              return (
                <TaskList
                  key={column.id}
                  title={column.id}
                  tasks={columnTasks}
                />
              );
            })}
          </div>
        </div>
      </main>
    </div>
  );
}
