import dayjs from "dayjs";

export function formatDate(date) {
  return dayjs(date).format("DD MMMM YYYY");
}
