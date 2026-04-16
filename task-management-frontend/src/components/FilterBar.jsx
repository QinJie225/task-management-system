import "./FilterBar.css";
import { X } from "lucide-react";

export function FilterBar({ filters, onChange, onClear, assignees }) {
  const hasFilters = Object.values(filters).some((val) => val !== "");

  return (
    <div className="filter-bar">
      <span className="filter-label">Filter by</span>

      <select
        value={filters.priority}
        onChange={(e) => onChange("priority", e.target.value)}
      >
        <option value="">All priorities</option>
        <option value="HIGH">High</option>
        <option value="MEDIUM">Medium</option>
        <option value="LOW">Low</option>
      </select>

      <select
        value={filters.createdBy}
        onChange={(e) => onChange("createdBy", e.target.value)}
      >
        <option value="">All assignees</option>
        {assignees.map((a) => (
          <option key={a} value={a}>
            {a}
          </option>
        ))}
      </select>

      {hasFilters && (
        <button onClick={onClear} className="">
          <X size={14}/> Clear filters
        </button>
      )}
    </div>
  );
}
