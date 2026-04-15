import { useState } from "react";
import { TASK_STATUS } from "../utils/constants.js";
import {
  ArrowLeft,
  Check,
  ChevronRight,
  PencilLine,
  Trash2,
  ChevronDown,
  X,
} from "lucide-react";

export function StatusDropdown({ value, onChange, disabled }) {
  const [open, setOpen] = useState(false);
  const status = TASK_STATUS[value];
  const { Icon } = status;

  return (
    <div className="status-select-wrapper">
      <button
        className="status-select-trigger"
        onClick={() => !disabled && setOpen((prev) => !prev)}
        disabled={disabled}
        style={{
          color: status.color,
          background: status.bg,
          border: `1px solid ${status.border}`,
        }}
      >
        <Icon size={12} />
        {status.label}
        {!disabled && <ChevronDown size={11} />}
      </button>

      {open && !disabled && (
        <div className="status-select-menu">
          {Object.keys(TASK_STATUS).map((s) => {
            const selectItem = TASK_STATUS[s];
            const { Icon: SIcon } = selectItem;
            return (
              <button
                key={s}
                className="status-select-item"
                onClick={() => {
                  onChange(s);
                  setOpen(false);
                }}
              >
                <SIcon size={13} color={selectItem.color} /> {s}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
