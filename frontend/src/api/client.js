import axios from "axios";

// Todas las llamadas pasan por el API Gateway, nunca directo a un microservicio.
const client = axios.create({
  baseURL: "http://localhost:8090",
  headers: { "Content-Type": "application/json" },
});

export default client;
