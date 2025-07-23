import util.*;
import tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        //создаем задачи
        Task task1 = new Task("task1", "description1"); // Без времени
        Task task2 = new Task(0, "task2", "description2", TaskStatus.NEW,
                LocalDateTime.of(2025, 7, 21, 8, 0),  // 8:00-9:00
                Duration.ofHours(1)
        );
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        Epic epic1 = taskManager.createEpic(new Epic(0, "epic1", "descriptionOfEpic1"));
        Epic epic2 = taskManager.createEpic(new Epic(0, "epic2", "descriptionOfEpic2"));

        // Изменяем временные интервалы подзадач, чтобы они не пересекались
        Subtask subtask1 = taskManager.createSubtask(new Subtask(0, "subtask1",
                "descriptionOfSubtask1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2025, 7, 21, 9, 30),  // 9:30-9:45 (после task2)
                Duration.ofMinutes(15)
        ));
        Subtask subtask2 = taskManager.createSubtask(new Subtask(0,
                "subtask2",
                "descriptionOfSubtask2",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2025, 7, 21, 10, 0),  // 10:00-11:00
                Duration.ofHours(1)
        ));
        Subtask subtask3 = taskManager.createSubtask(new Subtask(0,
                "subtask3",
                "descriptionOfSubtask3",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2025, 7, 21, 12, 0),  // 12:00-13:00
                Duration.ofHours(1)
        ));
        Subtask subtask4 = taskManager.createSubtask(new Subtask(0,
                "subtask4",
                "descriptionOfSubtask4",
                TaskStatus.NEW,
                epic2.getId(),
                LocalDateTime.of(2025, 7, 21, 14, 0),  // 14:00-14:25
                Duration.ofMinutes(25)
        ));

        // остальной код остается без изменений
        System.out.println(taskManager.getAllTasks());
        task1.setStatus(TaskStatus.DONE);
        task1.setDescription("ohhhhh");
        taskManager.updateTask(task1);
        taskManager.updateTask(new Task(task2.getId(),
                "task2changed",
                "descripChanged",
                TaskStatus.DONE,
                LocalDateTime.of(2025, 7, 21, 15, 0),  // Обновляем время для task2
                Duration.ofHours(1)
        ));
        System.out.println(taskManager.getAllEpics());
        System.out.println(taskManager.getAllSubtasks());
        taskManager.deleteSubtaskById(subtask1.getId());
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask2);
        taskManager.updateSubtask(new Subtask(subtask3.getId(),
                "titleChanged",
                "Changed",
                TaskStatus.DONE,
                epic1.getId(),
                LocalDateTime.of(2025, 7, 21, 11, 30), // обновленное время
                Duration.ofHours(1)
        ));
        taskManager.updateEpic(epic1);
        epic2.setStatus(TaskStatus.DONE);
        epic2.setDescription("new description");
        taskManager.updateEpic(epic2);

        taskManager.getTaskById(task1.getId());
        task1.setTitle("pupupu");
        taskManager.updateTask(task1);
        taskManager.getTaskById(task1.getId());
        task1.setTitle("pu-pu-pu");
        taskManager.updateTask(task1);
        taskManager.getTaskById(task1.getId());
        taskManager.getSubtaskById(subtask4.getId());
        taskManager.getEpicById(epic1.getId());
        taskManager.getEpicById(epic1.getId());
        taskManager.updateEpic(epic1);
        taskManager.getEpicById(epic1.getId());
        taskManager.getEpicById(epic2.getId());
        taskManager.getEpicById(epic1.getId());
        taskManager.getEpicById(epic2.getId());
        taskManager.getSubtaskById(subtask2.getId());
        taskManager.deleteEpicById(epic1.getId());
        taskManager.updateEpic(epic1);
        taskManager.getSubtaskById(subtask2.getId());
        task1.setTitle("last change");
        taskManager.updateTask(task1);
        taskManager.getTaskById(task1.getId());

        printAllTasks(taskManager);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        System.out.println();
        System.out.println("Приоритет задач:");
        prioritizedTasks.stream()
                .forEach(System.out::println);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        manager.getAllTasks().stream()
                .forEach(System.out::println);

        System.out.println();
        System.out.println("Эпики:");
        manager.getAllEpics().stream()
                .peek(System.out::println)
                .forEach(epic ->
                        manager.getSubtasksOfEpic(epic.getId()).stream()
                                .map(task -> "--> " + task)
                                .forEach(System.out::println)
                );
        System.out.println();
        System.out.println("Подзадачи:");
        manager.getAllSubtasks().stream()
                .forEach(System.out::println);
        System.out.println();
        System.out.println("История:");
        manager.getHistory().stream()
                .forEach(System.out::println);
    }
}