import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
  url: "http://localhost:8080",
  realm: "internship-task-realm",
  clientId: "task-frontend",
});

export default keycloak;