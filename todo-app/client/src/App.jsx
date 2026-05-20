import { useState, useEffect } from "react";

const API_URL = "http://localhost:8080";

export default function App() {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [newTitle, setNewTitle] = useState("");
  const [newDesc, setNewDesc] = useState("");
  const [adding, setAdding] = useState(false);
  const [filter, setFilter] = useState("all"); // all | active | done

  // ── Завантажити завдання з сервера ──────────────────────────
  const fetchTasks = async () => {
    try {
      setError(null);
      const res = await fetch(`${API_URL}/tasks`);
      if (!res.ok) throw new Error("Сервер повернув помилку");
      const data = await res.json();
      setTasks(data);
    } catch (e) {
      setError("Не вдалося з'єднатися з сервером. Переконайтесь, що TodoServer запущено.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchTasks(); }, []);

  // ── Додати завдання ─────────────────────────────────────────
  const handleAdd = async () => {
    if (!newTitle.trim()) return;
    setAdding(true);
    try {
      const res = await fetch(`${API_URL}/tasks`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title: newTitle.trim(), description: newDesc.trim() }),
      });
      if (!res.ok) throw new Error();
      const task = await res.json();
      setTasks(prev => [...prev, task]);
      setNewTitle("");
      setNewDesc("");
    } catch {
      setError("Помилка при додаванні завдання");
    } finally {
      setAdding(false);
    }
  };

  // ── Перемкнути виконання ────────────────────────────────────
  const handleToggle = async (task) => {
    try {
      const res = await fetch(`${API_URL}/tasks/${task.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title: task.title,
          description: task.description,
          completed: !task.completed,
        }),
      });
      if (!res.ok) throw new Error();
      const updated = await res.json();
      setTasks(prev => prev.map(t => t.id === task.id ? updated : t));
    } catch {
      setError("Помилка при оновленні завдання");
    }
  };

  // ── Видалити завдання ───────────────────────────────────────
  const handleDelete = async (id) => {
    try {
      const res = await fetch(`${API_URL}/tasks/${id}`, { method: "DELETE" });
      if (!res.ok) throw new Error();
      setTasks(prev => prev.filter(t => t.id !== id));
    } catch {
      setError("Помилка при видаленні завдання");
    }
  };

  const filtered = tasks.filter(t =>
    filter === "all" ? true : filter === "done" ? t.completed : !t.completed
  );
  const doneCount = tasks.filter(t => t.completed).length;

  return (
    <div style={styles.page}>
      <div style={styles.container}>
        {/* Header */}
        <div style={styles.header}>
          <h1 style={styles.title}>
            <span style={styles.titleIcon}>✓</span> Список завдань
          </h1>
          <p style={styles.subtitle}>
            {doneCount} з {tasks.length} виконано
          </p>
        </div>

        {/* Error */}
        {error && (
          <div style={styles.errorBox}>
            ⚠ {error}
            <button style={styles.closeBtn} onClick={() => setError(null)}>✕</button>
          </div>
        )}

        {/* Add form */}
        <div style={styles.addForm}>
          <input
            style={styles.input}
            placeholder="Назва завдання *"
            value={newTitle}
            onChange={e => setNewTitle(e.target.value)}
            onKeyDown={e => e.key === "Enter" && handleAdd()}
          />
          <input
            style={styles.input}
            placeholder="Опис (необов'язково)"
            value={newDesc}
            onChange={e => setNewDesc(e.target.value)}
            onKeyDown={e => e.key === "Enter" && handleAdd()}
          />
          <button
            style={{
              ...styles.addBtn,
              opacity: adding || !newTitle.trim() ? 0.6 : 1,
              cursor: adding || !newTitle.trim() ? "not-allowed" : "pointer",
            }}
            onClick={handleAdd}
            disabled={adding || !newTitle.trim()}
          >
            {adding ? "Додаємо..." : "+ Додати"}
          </button>
        </div>

        {/* Filter tabs */}
        <div style={styles.filters}>
          {[["all", "Усі"], ["active", "Активні"], ["done", "Виконані"]].map(([key, label]) => (
            <button
              key={key}
              style={{
                ...styles.filterBtn,
                ...(filter === key ? styles.filterBtnActive : {}),
              }}
              onClick={() => setFilter(key)}
            >
              {label}
            </button>
          ))}
        </div>

        {/* Task list */}
        <div style={styles.list}>
          {loading ? (
            <div style={styles.empty}>Завантаження...</div>
          ) : filtered.length === 0 ? (
            <div style={styles.empty}>
              {filter === "done" ? "Немає виконаних завдань" :
               filter === "active" ? "Немає активних завдань" :
               "Завдань немає. Додайте перше!"}
            </div>
          ) : (
            filtered.map(task => (
              <div key={task.id} style={{
                ...styles.taskCard,
                ...(task.completed ? styles.taskCardDone : {}),
              }}>
                <button
                  style={{
                    ...styles.checkbox,
                    ...(task.completed ? styles.checkboxDone : {}),
                  }}
                  onClick={() => handleToggle(task)}
                  title={task.completed ? "Позначити активним" : "Позначити виконаним"}
                >
                  {task.completed ? "✓" : ""}
                </button>
                <div style={styles.taskInfo}>
                  <div style={{
                    ...styles.taskTitle,
                    ...(task.completed ? styles.taskTitleDone : {}),
                  }}>
                    {task.title}
                  </div>
                  {task.description && (
                    <div style={styles.taskDesc}>{task.description}</div>
                  )}
                  <div style={styles.taskDate}>🕐 {task.createdAt}</div>
                </div>
                <button
                  style={styles.deleteBtn}
                  onClick={() => handleDelete(task.id)}
                  title="Видалити завдання"
                >
                  ✕
                </button>
              </div>
            ))
          )}
        </div>

        {/* Footer */}
        <div style={styles.footer}>
          REST API · TCP Socket Server · Java + React
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    background: "linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%)",
    display: "flex",
    alignItems: "flex-start",
    justifyContent: "center",
    padding: "40px 16px",
    fontFamily: "'Segoe UI', system-ui, sans-serif",
  },
  container: {
    width: "100%",
    maxWidth: "680px",
  },
  header: {
    textAlign: "center",
    marginBottom: "32px",
  },
  title: {
    color: "#f1f5f9",
    fontSize: "2.2rem",
    fontWeight: "700",
    margin: "0 0 8px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    gap: "12px",
  },
  titleIcon: {
    background: "#3b82f6",
    color: "#fff",
    width: "44px",
    height: "44px",
    borderRadius: "12px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "1.4rem",
  },
  subtitle: {
    color: "#94a3b8",
    margin: 0,
    fontSize: "0.95rem",
  },
  errorBox: {
    background: "#451a0a",
    border: "1px solid #dc2626",
    color: "#fca5a5",
    borderRadius: "10px",
    padding: "12px 16px",
    marginBottom: "20px",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    fontSize: "0.9rem",
  },
  closeBtn: {
    background: "none",
    border: "none",
    color: "#fca5a5",
    cursor: "pointer",
    fontSize: "1rem",
    padding: "0 4px",
  },
  addForm: {
    background: "#1e293b",
    border: "1px solid #334155",
    borderRadius: "14px",
    padding: "20px",
    marginBottom: "20px",
    display: "flex",
    flexDirection: "column",
    gap: "12px",
  },
  input: {
    background: "#0f172a",
    border: "1px solid #334155",
    borderRadius: "8px",
    padding: "12px 16px",
    color: "#f1f5f9",
    fontSize: "0.95rem",
    outline: "none",
    transition: "border-color 0.2s",
  },
  addBtn: {
    background: "#3b82f6",
    border: "none",
    borderRadius: "8px",
    padding: "12px",
    color: "#fff",
    fontWeight: "600",
    fontSize: "0.95rem",
    transition: "background 0.2s",
  },
  filters: {
    display: "flex",
    gap: "8px",
    marginBottom: "16px",
  },
  filterBtn: {
    background: "#1e293b",
    border: "1px solid #334155",
    borderRadius: "8px",
    padding: "8px 18px",
    color: "#94a3b8",
    cursor: "pointer",
    fontSize: "0.9rem",
    transition: "all 0.2s",
  },
  filterBtnActive: {
    background: "#3b82f6",
    border: "1px solid #3b82f6",
    color: "#fff",
  },
  list: {
    display: "flex",
    flexDirection: "column",
    gap: "10px",
  },
  taskCard: {
    background: "#1e293b",
    border: "1px solid #334155",
    borderRadius: "12px",
    padding: "16px",
    display: "flex",
    alignItems: "flex-start",
    gap: "14px",
    transition: "border-color 0.2s",
  },
  taskCardDone: {
    opacity: 0.65,
    borderColor: "#1e293b",
  },
  checkbox: {
    width: "26px",
    height: "26px",
    minWidth: "26px",
    borderRadius: "7px",
    border: "2px solid #475569",
    background: "transparent",
    cursor: "pointer",
    color: "#3b82f6",
    fontWeight: "bold",
    fontSize: "0.9rem",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
  },
  checkboxDone: {
    background: "#3b82f6",
    border: "2px solid #3b82f6",
    color: "#fff",
  },
  taskInfo: {
    flex: 1,
    minWidth: 0,
  },
  taskTitle: {
    color: "#f1f5f9",
    fontWeight: "600",
    fontSize: "1rem",
    marginBottom: "4px",
  },
  taskTitleDone: {
    textDecoration: "line-through",
    color: "#64748b",
  },
  taskDesc: {
    color: "#94a3b8",
    fontSize: "0.85rem",
    marginBottom: "6px",
  },
  taskDate: {
    color: "#475569",
    fontSize: "0.78rem",
  },
  deleteBtn: {
    background: "transparent",
    border: "none",
    color: "#475569",
    cursor: "pointer",
    fontSize: "1rem",
    padding: "2px 6px",
    borderRadius: "6px",
    transition: "color 0.2s",
  },
  empty: {
    textAlign: "center",
    color: "#475569",
    padding: "40px",
    fontSize: "0.95rem",
  },
  footer: {
    textAlign: "center",
    color: "#334155",
    fontSize: "0.78rem",
    marginTop: "32px",
  },
};
