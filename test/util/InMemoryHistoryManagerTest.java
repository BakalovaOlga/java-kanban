package util;

import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager = new InMemoryHistoryManager();

    @Test
    void historyManagerKeepPreviousTaskVersion() {
        Task task = new Task(0, "Inital title", "Inital description", TaskStatus.NEW);
        historyManager.add(task);

        task.setTitle("New title");
        task.setDescription("some description");
        task.setStatus(TaskStatus.IN_PROGRESS);

        assertNotEquals(historyManager.getHistory().getFirst().getTitle(), task.getTitle(), "Title is equals");
        assertNotEquals(historyManager.getHistory().getFirst().getDescription(), task.getDescription());
        assertNotEquals(historyManager.getHistory().getFirst().getStatus(), task.getStatus());
    }

}