package util;

import exceptions.ManagerSaveException;
import org.junit.jupiter.api.*;
import tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            tempFile = File.createTempFile("tasks", ".csv");
            return new FileBackedTaskManager(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл", e);
        }
    }

    @AfterEach
    public void tearDown() {
        if (tempFile != null) {
            try {
                Files.deleteIfExists(tempFile.toPath());
            } catch (IOException e) {
                System.err.println("Не удалось удалить временный файл: " + e.getMessage());
            }
        }
    }


    @Test
    void testSaveAndLoadEmptyManager() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loaded.getAllTasks().isEmpty());
        assertTrue(loaded.getAllEpics().isEmpty());
        assertTrue(loaded.getAllSubtasks().isEmpty());
    }

    @Test
    void testSaveAndLoadMultipleTasks() {
        // Создаем задачи с явным указанием времени
        Task task = new Task(0, "Task 1", "Description 1", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofHours(1));
        taskManager.createTask(task);

        Epic epic1 = new Epic("Epic 1", "Epic Description 1");
        taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Sub Description 1", TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.now().plusHours(2),
                Duration.ofMinutes(30));
        taskManager.createSubtask(subtask1);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверки
        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());

        // Проверяем, что подзадача связана с эпиком
        Epic loadedEpic = loadedManager.getEpicById(epic1.getId());
        assertNotNull(loadedEpic);
        assertTrue(loadedEpic.getSubtaskId().contains(subtask1.getId()));
    }

    @Test
    void testLoadFromNonExistentFile() {
        Path nonExistentPath = Path.of("non_existent_file.csv");
        File nonExistentFile = nonExistentPath.toFile();
        try {
            FileBackedTaskManager.loadFromFile(nonExistentFile);
            System.out.println("Ожидалось исключение ManagerSaveException.");
        } catch (ManagerSaveException e) {
            System.out.println("Файл не существует.");
        }
    }
}