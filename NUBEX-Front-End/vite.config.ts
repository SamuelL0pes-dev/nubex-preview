import react from "@vitejs/plugin-react";
import { defineConfig } from "vitest/config";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": new URL("./src", import.meta.url).pathname,
    },
  },
  test: {
    environment: "jsdom",
    setupFiles: "./src/setupTests.ts",
    env: {
      VITE_API_USUARIOS: "http://localhost:8081",
      VITE_API_AGENDAMENTOS: "http://localhost:8082",
      VITE_API_CATALOGO: "http://localhost:8083",
      VITE_API_PROJETOS: "http://localhost:8084",
      VITE_API_CONTEUDO: "http://localhost:8085",
    },
  },
});
