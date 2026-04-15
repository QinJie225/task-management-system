import axios from "axios";
import keycloak from '../keycloak'

const axiosInstance = axios.create({
  baseURL: "http://localhost:8081/api", 
  headers: {
    "Content-Type": "application/json",
  },
});

axiosInstance.interceptors.request.use(async (config) => {
  await keycloak.updateToken(30).catch(() => keycloak.login());
  config.headers.Authorization = `Bearer ${keycloak.token}`; 
  return config;
});

export default axiosInstance;