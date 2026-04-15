import "./FilterBar.css";

export function FilterBar({ filters, onChange, onClear, assignees }) {
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

      {(filters.priority || filters.assignee) && (
        <button onClick={onClear}>✕ Clear filters</button>
      )}
    </div>
  );
}
