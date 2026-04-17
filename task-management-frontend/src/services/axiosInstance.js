// src/services/axiosInstance.js
import axios from "axios";

const axiosInstance = axios.create({
  baseURL: "http://localhost:8081/api",
  headers: {
    "Content-Type": "application/json",
  },
});

axiosInstance.interceptors.request.use((config) => {
  const raw = localStorage.getItem("ROCP_token"); 
  console.log("mumumumumu", raw) // PKCE library's key
  if (raw) {
    console.log("mimkmimimmiim", raw)
    const token = raw.replace(/^"|"$/g, "");
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default axiosInstance;