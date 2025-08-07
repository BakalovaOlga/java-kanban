package util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.Subtask;
import tasks.Epic;
import tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager(); // Реализация для InMemoryTaskManager
    }

    @BeforeEach
    void setUp() {
        super.setUp(); // Важно: вызываем родительский setUp()!
    }

    @AfterEach
    void clear() {
        taskManager.clearAll();
    }


    @Test
    void taskUnchangingAfterAdd() {
        Task task = new Task(0, "title", "description", TaskStatus.NEW,
                LocalDateTime.of(2025, 7, 23, 10, 0), // обновленное время
                Duration.ofHours(1));
        taskManager.createTask(task);
        Task task2 = taskManager.getTaskById(task.getId());

        assertEquals(task.getId(), task2.getId());
        assertEquals(task.getDescription(), task2.getDescription());
        assertEquals(task.getTitle(), task2.getTitle());
        assertEquals(task.getStatus(), task2.getStatus());

    }

    @Test
    public void testPrioritizedTasks() {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(45);

        Task task1 = new Task("Task 1", "Description 1");
        task1.setStartTime(now.plusHours(3));
        task1.setDuration(duration);
        taskManager.createTask(task1);

        Task task2 = new Task("Task 2", "Description 2");
        task2.setStartTime(now);
        task2.setDuration(duration);
        taskManager.createTask(task2);

        Task task3 = new Task("Task 3", "Description 3");
        task3.setStartTime(now.plusHours(2));
        task3.setDuration(duration);
        taskManager.createTask(task3);

        List<Task> prioritized = List.copyOf(taskManager.getPrioritizedTasks());
        assertEquals(3, prioritized.size(), "Неверное количество задач в списке приоритетов.");
        assertEquals(task2, prioritized.get(0), "Первая задача должна быть task2.");
        assertEquals(task3, prioritized.get(1), "Вторая задача должна быть task3.");
        assertEquals(task1, prioritized.get(2), "Третья задача должна быть task1.");
    }

}