import { taskApi } from "../../services/apiService.js";
import { useState, useEffect } from "react";
import { formatDateTime } from "../../utils/formatDateTime.js";
import { formatAvatarIcon } from "../../utils/formatAvatarIcon.js";
import dayjs from "dayjs";
import { Link, Outlet } from "react-router-dom";
import { useLoaderData } from "react-router-dom";
import "./TaskDetailsPage.css";
import {
  ArrowLeft,
  Check,
  ChevronRight,
  PencilLine,
  Trash2,
  ChevronDown,
  X,
} from "lucide-react";
import { useTaskFields } from "../../hooks/useTaskFields";
import { StatusDropdown } from "../../components/StatusDropdown.jsx";
import { PriorityDropdown } from "../../components/PriorityDropdown.jsx";
import { updateTaskSchema } from "../../utils/taskSchema";
import { useAuthData } from "../../hooks/useAuthData";

export async function updateTaskAction({ request, params }) {
  const formData = await request.formData();
  const data = Object.fromEntries(formData);

  const result = updateTaskSchema.safeParse(data);
  if (!result.success) {
    return { errors: result.error.flatten().fieldErrors };
  }

  await taskApi.updateTask(params.taskId, data);
  return { success: true };
}

export function TaskDetailsPage() {
  const { canModify: canModifyTask } = useAuthData();
  const task = useLoaderData();
  const canModify = canModifyTask(task)
  const {
    fields,
    setFields,
    handleChange,
    revertField,
    fetcher,
    errors,
    isRefetching,
  } = useTaskFields(task);
  const [isEditing, setIsEditing] = useState(false);
  const [isEditingTitle, setIsEditingTitle] = useState(false);

  if (!fields) return <p>Loading...</p>;

  const handleSave = () => {
    handleChange("description", fields.description);
  };

  const handleCancel = () => {
    revertField("description");
    setIsEditing(false);
  };

  const handleSaveTitle = () => {
    handleChange("title", fields.title);
  };

  const handleCancelTitle = () => {
    revertField("title");
    setIsEditingTitle(false);
  };

  useEffect(() => {
    if (fetcher.data?.success === true) {
      setIsEditing(false);
      setIsEditingTitle(false);
    }
  }, [fetcher.data]);

  return (
    <>
      <div className="task-details-topbar">
        <nav className="breadcrumb-navigation-section">
          <Link to="/" className="breadcrumb-back-btn">
            <ArrowLeft size={25} />
          </Link>
          <span className="breadcrumb-text">Task Dashboard</span>
          <ChevronRight size={20} className="breadcrumb-sep" />
          <span className="breadcrumb-title">{fields.title}</span>
        </nav>

        <div className="task-details-topbar-actions">
          {fetcher.state === "submitting" && (
            <span className="saved-badge">Saving...</span>
          )}
          {fetcher.state === "idle" && fetcher.data?.success === true && (
            <span className="saved-badge">
              <Check size={10} /> Saved
            </span>
          )}
          {canModify && (
            <Link to={`/tasks/${task.taskId}/delete`} className="danger-btn">
              <Trash2 size={13} /> Delete
            </Link>
          )}
        </div>
      </div>
      <div className="task-details-body">
        <div className="task-details-layout">
          <div className="task-details-left">
            <div className="task-title-wrapper">
              {isEditingTitle ? (
                <>
                  <input
                    className="task-title-input"
                    value={fields.title}
                    onChange={(e) =>
                      setFields((prev) => ({
                        ...prev,
                        title: e.target.value,
                      }))
                    }
                    onKeyDown={(e) => {
                      if (e.key === "Enter") handleSaveTitle();
                      if (e.key === "Escape") handleCancelTitle();
                    }}
                    autoFocus
                  />

                  {errors?.title && (
                    <span className="field-error">{errors.title[0]}</span>
                  )}

                  <div className="action-buttons">
                    <button className="primary-btn" onClick={handleSaveTitle}>
                      <Check size={10} /> Save
                    </button>
                    <button
                      className="secondary-btn"
                      onClick={handleCancelTitle}
                    >
                      <X size={13} />
                    </button>
                  </div>
                </>
              ) : (
                <div className="task-title-row">
                  <h1
                    className="task-title"
                    onClick={() => canModify && setIsEditingTitle(true)}
                  >
                    {fields.title}
                  </h1>
                  {canModify && (
                    <PencilLine
                      size={14}
                      className="task-title-edit-icon"
                      onClick={() => setIsEditingTitle(true)}
                    />
                  )}
                </div>
              )}
            </div>

            <div className="task-description-section">
              <div className="section-header">
                <h4>Description</h4>
                {!isEditing && canModify && (
                  <button
                    className="edit-btn"
                    onClick={() => setIsEditing(true)}
                  >
                    <PencilLine size={14} /> Edit
                  </button>
                )}
              </div>

              {isEditing && canModify ? (
                <div className="edit-container">
                  <textarea
                    className="task-textarea"
                    value={fields.description}
                    onChange={(e) =>
                      setFields((prev) => ({
                        ...prev,
                        description: e.target.value,
                      }))
                    }
                    autoFocus
                  ></textarea>
                  {errors?.description && (
                    <span className="field-error">{errors.description[0]}</span>
                  )}
                  <div className="action-buttons">
                    <button className="primary-btn" onClick={handleSave}>
                      Save
                    </button>
                    <button className="secondary-btn" onClick={handleCancel}>
                      Cancel
                    </button>
                  </div>
                </div>
              ) : (
                <div
                  className="description-display"
                  onClick={() => setIsEditing(true)}
                >
                  {fields.description.split("\n").map((line, i) => (
                    <p key={i}>{line}</p>
                  ))}
                </div>
              )}
            </div>
          </div>

          <aside className="task-details-right">
            <div className="details-header">
              <span className="details-header-title">Details</span>
            </div>

            <div className="details-row">
              <span className="details-label">Status</span>
              <div className="details-value">
                <StatusDropdown
                  value={fields.status}
                  onChange={(e) => handleChange("status", e)}
                  disabled={!canModify}
                />
              </div>
            </div>

            <div className="details-row">
              <span className="details-label">Priority</span>
              <div className="details-value">
                <PriorityDropdown
                  value={fields.priority}
                  onChange={(e) => handleChange("priority", e)}
                  disabled={!canModify}
                />
              </div>
            </div>

            <div className="details-row">
              <span className="details-label">Due date</span>
              <div className="details-value">
                <input
                  className="date-input"
                  type="date"
                  value={dayjs(fields.dueDate).format("YYYY-MM-DD")}
                  onChange={(e) => {
                    canModify && handleChange("dueDate", e.target.value);
                  }}
                  onBlur={() => {
                    if (errors?.dueDate) revertField("dueDate");
                  }}
                  disabled={!canModify}
                ></input>

                {errors?.dueDate && (
                  <span className="field-error">{errors.dueDate[0]}</span>
                )}
              </div>
            </div>

            <div className="details-row">
              <span className="details-label">Reporter</span>
              <div className="details-value">
                <div className="mini-avatar">
                  {formatAvatarIcon(fields.createdBy)}
                </div>
                {fields.createdBy}
              </div>
            </div>

            <div className="details-row">
              <span className="details-label">Updated By</span>
              <div className="details-value">
                <div className="mini-avatar">
                  {formatAvatarIcon(fields.updatedBy)}
                </div>
                {fields.updatedBy}
              </div>
            </div>

            <div className="details-sidebar-meta">
              <div className="sidebar-metaline">
                Created {formatDateTime(fields.createdAt)}
              </div>

              <div className="sidebar-metaline">
                {isRefetching ? (
                  <span style={{ color: "#94a3b8" }}>Updating...</span>
                ) : (
                  <>Updated {formatDateTime(fields.updatedAt)}</>
                )}
              </div>
            </div>
          </aside>
        </div>
      </div>

      <Outlet />
    </>
  );
}
