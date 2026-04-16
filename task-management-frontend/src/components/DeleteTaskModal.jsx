import { Trash2, AlertTriangle } from "lucide-react";
import "./DeleteTaskModal.css";
import { Link, Form, redirect} from "react-router-dom";
import { taskApi } from "../services/apiService";
import { showToast } from "../utils/toastConfig";


export function DeleteTaskModal() {
  return (
    <div className="modal-overlay">
      <div className="delete-modal">
        <div className="delete-modal-icon">
          <AlertTriangle size={12} />
        </div>
        <h3 className="delete-modal-title">Delete this task?</h3>
        <p className="delete-modal-body">
          This task will be permanently deleted. This cannot be undone.
        </p>
        <div className="delete-modal-actions">
          <Link to=".." relative="path" className="secondary-btn">
            Cancel
          </Link>
          <Form method="post">
            <button type="submit" className="danger-btn">
              <Trash2 size={13} /> Delete task
            </button>
          </Form>
        </div>
      </div>
    </div>
  );
}

export async function deleteTaskAction({ params }) {
  try {
    await taskApi.deleteTask(params.taskId);
    showToast.success("Success!", "Task deletion is being processed.");
    return redirect("/");
  } catch (error) {
    showToast.error("Error", "Failed to delete task. Please try again.");
    return { error: "Failed to delete task" };
  }
}