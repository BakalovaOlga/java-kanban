package util;

import exceptions.ManagerSaveException;
import org.junit.jupiter.api.*;
import tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;
    private Task task;
    private Epic epic1;
    private Subtask subtask1;
    private Subtask subtask2;


    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
        tempFile.deleteOnExit();
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
        // Создаем задачи
        task = new Task("Task 1", "Description 1");
        manager.createTask(task);
        epic1 = new Epic("Epic 1", "Epic Description 1");
        manager.createEpic(epic1);
        subtask1 = new Subtask("Subtask 1", "Sub Description 1", epic1.getId());
        manager.createSubtask(subtask1);
        subtask2 = new Subtask("Subtask 2", "Sub Description 2", epic1.getId());
        manager.createSubtask(subtask2);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем загруженные задачи
        List<Task> loadedTasks = loadedManager.getAllTasks();
        List<Epic> loadedEpics = loadedManager.getAllEpics();
        List<Subtask> loadedSubtasks = loadedManager.getAllSubtasks();

        assertEquals(1, loadedTasks.size());
        assertEquals(1, loadedEpics.size());
        assertEquals(2, loadedSubtasks.size());

        // Проверяем, что подзадачи связаны с эпиком
        Epic loadedEpic = loadedEpics.getFirst();
        assertTrue(loadedEpic.getSubtaskId().contains(subtask1.getId()));
        assertTrue(loadedEpic.getSubtaskId().contains(subtask2.getId()));
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