package com.todo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TodoServer {

    final int PORT =private static  8080;
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        TaskStore store = new TaskStore();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║     Todo REST Server запущено!       ║");
        System.out.printf ("║     Порт: %-28s║%n", PORT);
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();
        System.out.println("Доступні маршрути:");
        System.out.println("  GET    http://localhost:" + PORT + "/tasks");
        System.out.println("  POST   http://localhost:" + PORT + "/tasks");
        System.out.println("  PUT    http://localhost:" + PORT + "/tasks/{id}");
        System.out.println("  DELETE http://localhost:" + PORT + "/tasks/{id}");
        System.out.println();
        System.out.println("Очікування підключень...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // Нескінченний цикл прийому підключень
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Обробка кожного клієнта в окремому потоці
                executor.submit(new ClientHandler(clientSocket, store));
            }
        } catch (IOException e) {
            System.err.println("Помилка сервера: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
