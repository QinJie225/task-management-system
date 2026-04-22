import { useState, useCallback } from "react";

export function usePagination(initialPage = 0, initialSize = 10) {
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [pageSize] = useState(initialSize);

  const goToPage = useCallback((page) => setCurrentPage(page), []);
  const goToNext = useCallback(
    (totalPages) => setCurrentPage((p) => Math.min(p + 1, totalPages - 1)),
    [],
  );
  const goToPrevious = useCallback(
    () => setCurrentPage((p) => Math.max(p - 1, 0)),
    [],
  );
  const goToFirst = useCallback(() => setCurrentPage(0), []);
  const goToLast = useCallback(
    (totalPages) => setCurrentPage(totalPages - 1),
    [],
  );

  return { currentPage, pageSize, goToPage, goToNext, goToPrevious, goToFirst, goToLast };
}
