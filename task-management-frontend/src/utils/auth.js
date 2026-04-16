import keycloak from "../keycloak";
import { taskApi } from "../services/apiService";

export function getAuthData() {
  const roles = keycloak.tokenParsed?.realm_access?.roles ?? [];
  const isAdmin = roles.includes("ADMIN");
  const isUser = roles.includes("USER");

  return {
    name: keycloak.tokenParsed?.name,
    username: keycloak.tokenParsed?.preferred_username,
    email: keycloak.tokenParsed?.email,
    roles,
    isAdmin,
    isUser,
    isAuthorized: isAdmin || isUser,
    logout: () => keycloak.logout({ redirectUri: window.location.origin }),
  };
}

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

export const canUserModifyTask = (task) => {
  const { username, isAdmin } = getAuthData();
  if (!task) return false;
  return isAdmin || task.createdBy === username;
};

export const requireAuth = () => {
  const { isAuthorized } = getAuthData();
  if (!isAuthorized) {
    throw new Response("Unauthorized", { status: 401 });
  }
};
