package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void getDefaultTaskManagerIsInitialized() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager);
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void getDefaultHistoryManagerIsInitialized() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager);
        assertTrue(historyManager.getHistory().isEmpty());
    }
}