import { Form, redirect, Link, useActionData } from "react-router-dom";
import { taskApi } from "../../services/apiService";
import { X } from "lucide-react";
import "./CreateTaskModal.css";
import { createTaskSchema } from "../../utils/taskSchema";
import { showToast } from "../../utils/toastConfig";

export function CreateTaskModal() {
  const actionData = useActionData();
  const errors = actionData?.errors;
  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <header className="modal-header">
          <h2>Create Task</h2>
          <Link to="/" className="close-btn">
            <X size={20} />
          </Link>
        </header>

        <Form method="post" id="create-task-form" className="modal-body">
          <div className="form-group">
            <label>Task Title</label>
            <input
              type="text"
              name="title"
              placeholder="What needs to be done?"
            />
            {errors?.title && (
              <span className="field-error">{errors.title[0]}</span>
            )}
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Priority</label>
              <select name="priority">
                <option value="">Select priority</option>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
              {errors?.priority && (
                <span className="field-error">{errors.priority[0]}</span>
              )}
            </div>
            <div className="form-group">
              <label>Due Date</label>
              <input type="date" name="dueDate" />
              {errors?.dueDate && (
                <span className="field-error">{errors.dueDate[0]}</span>
              )}
            </div>
          </div>
          <div className="form-group">
            <label>Description</label>
            <textarea
              rows="4"
              name="description"
              placeholder="Add more details..."
            ></textarea>
            {errors?.description && (
              <span className="field-error">{errors.description[0]}</span>
            )}
          </div>
        </Form>

        <footer className="modal-footer">
          <Link to="/" className="cancel-btn">
            Cancel
          </Link>
          <button
            type="submit"
            form="create-task-form"
            className="create-submit-btn"
          >
            Create Task
          </button>
        </footer>
      </div>
    </div>
  );
}

export async function createTaskAction({ request }) {
  const formData = await request.formData();
  const data = Object.fromEntries(formData);

  const result = createTaskSchema.safeParse(data);
  if (!result.success) {
    return { errors: result.error.flatten().fieldErrors };
  }

  try {
    await taskApi.createTask(result.data);
    showToast.success("Success!", "Task created successfully.");
    return redirect("/");
  } catch (error) {
    showToast.error("Error", "Failed to create task. Please try again.");
    return { error: "Failed to create task" };
  }
}
