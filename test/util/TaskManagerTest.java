package util;

import exceptions.TimeConflictException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
    }

    @AfterEach
    void clear() {
        taskManager.clearAll();
    }

    @Test
    public void testCreateAndGetTask() {
        Task task = new Task("Test Task", "Test Description");
        taskManager.createTask(task);

        Task savedTask = taskManager.getTaskById(task.getId());

        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task, savedTask, "Задачи не совпадают");

        List<Task> tasks = taskManager.getAllTasks();
        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(1, tasks.size(), "Неверное количество задач");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают");
    }

    @Test
    void createAllTypesAndGetById() {
        Task task = new Task(0, "title", "description", TaskStatus.NEW,
                LocalDateTime.of(2025, 7, 21, 15, 30), // обновленное время
                Duration.ofHours(2));
        taskManager.createTask(task);
        Epic epic = new Epic("title", "description");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask(0, "title", "description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 7, 21, 18, 30), // обновленное время
                Duration.ofHours(1));
        taskManager.createSubtask(subtask);

        assertEquals(task, taskManager.getTaskById(task.getId()), "Задачи не совпадают");
        assertEquals(epic, taskManager.getEpicById(epic.getId()), "Эпики не совпадают");
        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()), "Подзадачи не совпадают");

    }

    @Test
    void clearAllTypes() {
        Task task = new Task(0, "title", "description", TaskStatus.NEW,
                LocalDateTime.of(2025, 7, 21, 11, 0), // обновленное время
                Duration.ofHours(1));
        taskManager.createTask(task);
        Epic epic = new Epic("title", "description");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask(0, "title", "description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 7, 21, 12, 30), // обновленное время
                Duration.ofHours(1));
        taskManager.createSubtask(subtask);

        taskManager.clearAllTasks();
        taskManager.deleteAllSubtasks();
        taskManager.clearAllEpics();


        assertTrue(taskManager.getAllTasks().isEmpty());
        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void deleteSubtaskByIdShouldNotLeaveIdsInEpic() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("Подзадача", "Описание");
        subtask.setEpicId(epicId);
        taskManager.createSubtask(subtask);
        int subtaskId = subtask.getId();

        taskManager.deleteSubtaskById(subtaskId);

        assertTrue(taskManager.getEpicById(epicId).getSubtaskId().isEmpty());

    }

    @Test
    public void testDeleteEpicWithSubtasks() {
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Test Description", epic.getId());
        taskManager.createSubtask(subtask);

        taskManager.deleteEpicById(epic.getId());

        assertNull(taskManager.getEpicById(epic.getId()), "Эпик не удален");
        assertNull(taskManager.getSubtaskById(subtask.getId()), "Подзадача не удалена вместе с эпиком");
        assertTrue(taskManager.getAllEpics().isEmpty(), "Список эпиков должен быть пуст");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пуст");
    }

    @Test
    public void testTimeConflict() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofHours(1);

        Task task1 = new Task("Task 1", "Description 1");
        task1.setStartTime(startTime);
        task1.setDuration(duration);
        taskManager.createTask(task1);

        Task task2 = new Task("Task 2", "Description 2");
        task2.setStartTime(startTime.plusMinutes(30)); // Пересекается с task1
        task2.setDuration(duration);

        assertThrows(TimeConflictException.class, () -> taskManager.createTask(task2),
                "Должно быть исключение при пересечении времени");
    }

    @Test
    public void testEpicTimeCalculation() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofHours(1);

        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId());
        subtask1.setStartTime(startTime);
        subtask1.setDuration(duration);
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId());
        subtask2.setStartTime(startTime.plusHours(2));
        subtask2.setDuration(duration);
        taskManager.createSubtask(subtask2);

        Epic savedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(startTime, savedEpic.getStartTime(), "Время начала эпика неверное");
        assertEquals(startTime.plusHours(3), savedEpic.getEndTime(), "Время окончания эпика неверное");
        assertEquals(Duration.ofHours(2), savedEpic.getDuration(), "Длительность эпика неверная");
    }

    @Test
    public void testEpicStatusCalculation() {
        Epic epic = new Epic("E", "D");
        taskManager.createEpic(epic);
        LocalDateTime start = LocalDateTime.now();

        // a. Все подзадачи NEW
        Subtask subtask1 = new Subtask(0, "S1", "D1", TaskStatus.NEW, epic.getId(),
                start, Duration.ofMinutes(30));
        taskManager.createSubtask(subtask1);
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Статус эпика должен быть NEW");

        // b. Все подзадачи DONE
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        assertEquals(TaskStatus.DONE, epic.getStatus(), "Статус эпика должен быть DONE");

        // c. NEW и DONE
        Subtask subtask2 = new Subtask(0, "S2", "D2", TaskStatus.NEW, epic.getId(),
                start.plusHours(1), Duration.ofMinutes(60));
        taskManager.createSubtask(subtask2);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS");

        // d. Подзадачи IN_PROGRESS
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask2);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS");
    }

}