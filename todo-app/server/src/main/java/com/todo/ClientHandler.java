package com.todo;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final TaskStore store;

    public ClientHandler(Socket socket, TaskStore store) {
        this.socket = socket;
        this.store = store;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            HttpRequest req = HttpRequest.parse(in);
            if (req == null) return;

            System.out.printf("[%s] %s %s%n",
                socket.getInetAddress().getHostAddress(),
                req.getMethod(), req.getPath());

            String response = route(req);
            out.print(response);
            out.flush();

        } catch (IOException e) {
            System.err.println("Handler error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private String route(HttpRequest req) {
        String path = req.getPath();
        String method = req.getMethod();

        // Handle CORS preflight
        if ("OPTIONS".equals(method)) {
            return buildResponse(200, "OK", "");
        }

        // GET /tasks — отримати всі завдання
        if ("GET".equals(method) && "/tasks".equals(path)) {
            return buildResponse(200, "OK", store.toJson());
        }

        // POST /tasks — додати нове завдання
        if ("POST".equals(method) && "/tasks".equals(path)) {
            String title = req.getJsonField("title");
            String description = req.getJsonField("description");
            if (title == null || title.isBlank()) {
                return buildResponse(400, "Bad Request", "{\"error\":\"Поле title є обов'язковим\"}");
            }
            Task task = store.addTask(title, description != null ? description : "");
            return buildResponse(201, "Created", task.toJson());
        }

        // PUT /tasks/{id} — оновити завдання
        if ("PUT".equals(method) && path.startsWith("/tasks/")) {
            try {
                int id = Integer.parseInt(path.substring(7));
                String title = req.getJsonField("title");
                String description = req.getJsonField("description");
                String completedStr = req.getJsonField("completed");
                Boolean completed = completedStr != null ? Boolean.parseBoolean(completedStr) : null;

                return store.updateTask(id, title, description, completed)
                    .map(t -> buildResponse(200, "OK", t.toJson()))
                    .orElse(buildResponse(404, "Not Found", "{\"error\":\"Завдання не знайдено\"}"));
            } catch (NumberFormatException e) {
                return buildResponse(400, "Bad Request", "{\"error\":\"Невірний ID\"}");
            }
        }

        // DELETE /tasks/{id} — видалити завдання
        if ("DELETE".equals(method) && path.startsWith("/tasks/")) {
            try {
                int id = Integer.parseInt(path.substring(7));
                if (store.deleteTask(id)) {
                    return buildResponse(200, "OK", "{\"message\":\"Завдання видалено\"}");
                } else {
                    return buildResponse(404, "Not Found", "{\"error\":\"Завдання не знайдено\"}");
                }
            } catch (NumberFormatException e) {
                return buildResponse(400, "Bad Request", "{\"error\":\"Невірний ID\"}");
            }
        }

        return buildResponse(404, "Not Found", "{\"error\":\"Маршрут не знайдено\"}");
    }

    private String buildResponse(int statusCode, String statusText, String body) {
        String cors =
            "Access-Control-Allow-Origin: *\r\n" +
            "Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n" +
            "Access-Control-Allow-Headers: Content-Type\r\n";

        return "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
               "Content-Type: application/json; charset=UTF-8\r\n" +
               "Content-Length: " + body.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + "\r\n" +
               cors +
               "Connection: close\r\n" +
               "\r\n" +
               body;
    }
}
