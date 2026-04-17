import { taskApi } from "../services/apiService";

function getParsedToken() {
  try {
    const raw = localStorage.getItem("ROCP_token");       // library's storage key
    if (!raw) return null;
    const [, payload] = raw.split(".");                     // decode JWT payload
    return JSON.parse(atob(payload));
  } catch {
    return null;
  }
}

export function getAuthData() {
  const tokenData = getParsedToken();
  const roles = tokenData?.realm_access?.roles ?? [];
  const isAdmin = roles.includes("ADMIN");
  const isUser = roles.includes("USER");

  console.log("bilibilibli", roles)
  return {
    name: tokenData?.name,
    username: tokenData?.preferred_username,
    email: tokenData?.email,
    roles,
    isAdmin,
    isUser,
    isAuthorized: isAdmin || isUser,
  };
}


export const requireAuth = () => {
  const { isAuthorized } = getAuthData();
  console.log('blablabala', isAuthorized)
  if (!isAuthorized) {
    throw new Response("Unauthorized", { status: 401 });
  }
};

export const requireTaskOwnership = async ({ params }) => {
  const { isAuthorized, username, isAdmin } = getAuthData();

  if (!isAuthorized) {
    throw new Response("Unauthorized", { status: 401 });
  }

  const task = await taskApi.getTask(params.taskId);

  if (task.createdBy !== username && !isAdmin) {
    throw new Response("Forbidden", { status: 403 });
  }
  return { task };
};

