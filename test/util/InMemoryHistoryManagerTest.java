package util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    public void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Task 1", "Description 1");
        task1.setId(1);
        task2 = new Task("Task 2", "Description 2");
        task2.setId(2);
        task3 = new Task("Task 3", "Description 3");
        task3.setId(3);
    }


    @Test
    void addShouldMoveDuplicateToEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "Дубликаты не должны добавляться");
        assertEquals(task2, history.getFirst());
        assertEquals(task1, history.getLast());
    }

    @Test
    void addShouldSaveCopy() {
        Task originalTask = new Task(0, "original title", "original description", TaskStatus.NEW);
        historyManager.add(originalTask);

        originalTask.setStatus(TaskStatus.IN_PROGRESS);
        originalTask.setDescription("changes");

        Task fromHistoryTask = historyManager.getHistory().getFirst();
        assertNotEquals(originalTask.getDescription(), fromHistoryTask.getDescription(),
                "Должен хранить копии");
        assertNotEquals(originalTask.getStatus(), fromHistoryTask.getStatus());
    }

    @Test
    void historyShouldBeEmpty() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void addNullShouldBeIgnored() {
        historyManager.add(null);
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void removeNonExistingTaskShouldBeIgnored() {
        historyManager.add(task3);
        historyManager.remove(123);
        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void removeShouldDeleteTask() {
        historyManager.add(task1);
        historyManager.remove(task1.getId());
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void removeShouldDeleteFromHead() {
        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(task1.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.getFirst());
    }

    @Test
    void getHistoryShouldReturnInOrder() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

}