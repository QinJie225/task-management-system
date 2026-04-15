import dayjs from "dayjs";

export function formatDateTime(date) {
  return dayjs(date).format("DD MMM YYYY HH:mm A");
}
