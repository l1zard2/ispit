package com.todo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskStore {
    private final Map<Integer, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TaskStore() {
        // Seed with sample data
        addTask("Вивчити Java Sockets", "Опрацювати розділ про TCP-сокети");
        addTask("Написати лабораторну роботу", "Реалізувати клієнт-серверний застосунок");
        addTask("Підготувати презентацію", "Зробити слайди для захисту");
    }

    public Task addTask(String title, String description) {
        int id = idCounter.getAndIncrement();
        String now = LocalDateTime.now().format(FORMATTER);
        Task task = new Task(id, title, description, false, now);
        tasks.put(id, task);
        return task;
    }

    public List<Task> getAllTasks() {
        List<Task> list = new ArrayList<>(tasks.values());
        list.sort(Comparator.comparingInt(Task::getId));
        return list;
    }

    public Optional<Task> getTaskById(int id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public Optional<Task> updateTask(int id, String title, String description, Boolean completed) {
        Task task = tasks.get(id);
        if (task == null) return Optional.empty();
        if (title != null && !title.isBlank()) task.setTitle(title);
        if (description != null) task.setDescription(description);
        if (completed != null) task.setCompleted(completed);
        return Optional.of(task);
    }

    public boolean deleteTask(int id) {
        return tasks.remove(id) != null;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder("[");
        List<Task> list = getAllTasks();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).toJson());
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
