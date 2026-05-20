package com.todo;

public class Task {
    private int id;
    private String title;
    private String description;
    private boolean completed;
    private String createdAt;

    public Task() {}

    public Task(int id, String title, String description, boolean completed, String createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Manual JSON serialization (no external libs needed)
    public String toJson() {
        return String.format(
            "{\"id\":%d,\"title\":\"%s\",\"description\":\"%s\",\"completed\":%b,\"createdAt\":\"%s\"}",
            id,
            escapeJson(title),
            escapeJson(description),
            completed,
            createdAt
        );
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    @Override
    public String toString() { return toJson(); }
}
