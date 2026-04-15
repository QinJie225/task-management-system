import { useState } from "react";
import { TASK_PRIORITY } from "../utils/constants.js";
import {
  ArrowLeft,
  Check,
  ChevronRight,
  PencilLine,
  Trash2,
  ChevronDown,
  X,
} from "lucide-react";

export function PriorityDropdown({ value, onChange, disabled }) {
  const [open, setOpen] = useState(false);
  const priority = TASK_PRIORITY[value];

  return (
    <div className="status-select-wrapper">
      <button
        className="status-select-trigger"
        onClick={() => !disabled && setOpen((v) => !v)}
        disabled={disabled}
        style={{
          color: priority.color,
          border: `1px solid ${priority.color}`,
        }}
      >
        {priority.label}
        {!disabled && <ChevronDown size={11} />}
      </button>

      {open && !disabled && (
        <div className="status-select-menu">
          {Object.keys(TASK_PRIORITY).map((p) => {
            const item = TASK_PRIORITY[p];
            return (
              <button
                key={p}
                className="status-select-item"
                onClick={() => {
                  onChange(p);
                  setOpen(false);
                }}
                style={{ color: item.color }}
              >
                {item.label}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
