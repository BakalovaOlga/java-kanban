package util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.Subtask;
import tasks.Epic;
import tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
   private TaskManager taskManager = new InMemoryTaskManager();

  @AfterEach
  void clear() {
      taskManager.clearAll();
  }

    @Test
    void createAllTypesAndGetById() {
        Task task = new Task(0, "title", "description", TaskStatus.NEW);
        taskManager.createTask(task);
        Epic epic = new Epic( "title", "description");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask(0, "title", "description", TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(subtask);

        assertEquals(task, taskManager.getTaskById(task.getId()), "tasks is not equals");
        assertEquals(epic, taskManager.getEpicById(epic.getId()), "epics is not equals");
        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()), "Subtasks is not equals");
    }

    @Test
    void taskUnchangingAfterAdd() {
        Task task = new Task(0, "title", "description", TaskStatus.NEW);
        taskManager.createTask(task);
        Task task2 = taskManager.getTaskById(task.getId());

        assertEquals(task.getId(), task2.getId());
        assertEquals(task.getDescription(), task2.getDescription());
        assertEquals(task.getTitle(), task2.getTitle());
        assertEquals(task.getStatus(), task2.getStatus());
    }


    @Test
    void clearAllTypes() {
        Task task = new Task(0, "title", "description", TaskStatus.NEW);
        taskManager.createTask(task);
        Epic epic = new Epic( "title", "description");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask(0, "title", "description", TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(subtask);

        taskManager.clearAllTasks();
        taskManager.deleteAllSubtasks();
        taskManager.clearAllEpics();


        assertTrue(taskManager.getAllTasks().isEmpty());
        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getAllSubtasks().isEmpty());

    }
}