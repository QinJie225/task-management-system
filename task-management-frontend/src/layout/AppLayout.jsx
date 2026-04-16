import { useState } from "react";
import { useNavigate, Outlet } from "react-router-dom";
import dayjs from "dayjs";
import { LogOut, Plus, ChevronDown } from "lucide-react";
import { formatAvatarIcon } from "../utils/formatAvatarIcon";
import { getAuthData } from "../utils/auth";
import "./AppLayout.css";

export function AppLayout() {
  const { name, username, email, isAdmin, logout } = getAuthData();
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const navigate = useNavigate();
  const openUserMenu = () => {
    setIsUserMenuOpen(!isUserMenuOpen);
  };
  const handleLogout = () => {
    return logout();
  };
  const userInfo = {
    username: username,
    name: name || username || "User",
    email: email || "No email",
    role: isAdmin ? "Administrator" : "User",
  };

  return (
    <div className="app-container">
      <main className="main-content">
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
                  {formatAvatarIcon(userInfo.username)}
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

        <div className="page-body">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
