import util.*;
import tasks.*;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        //создаем задачи

        Task task1 = new Task("task1", "description1");
        Task task2 = new Task(0, "task2", "description2", TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        Epic epic1 = taskManager.createEpic(new Epic(0, "epic1", "descriptionOfEpic1"));
        Epic epic2 = taskManager.createEpic(new Epic(0, "epic2", "descriptionOfEpic2"));


        Subtask subtask1 = taskManager.createSubtask(new Subtask(0, "subtask1",
                "descriptionOfSubtask1",
                TaskStatus.NEW,
                epic1.getId()
        ));
        Subtask subtask2 = taskManager.createSubtask(new Subtask(0,
                "subtask2",
                "descriptionOfSubtask2",
                TaskStatus.NEW,
                epic1.getId()
        ));
        Subtask subtask3 = taskManager.createSubtask(new Subtask(0,
                "subtask3",
                "descriptionOfSubtask3",
                TaskStatus.NEW,
                epic1.getId()
        ));
        Subtask subtask4 = taskManager.createSubtask(new Subtask(0,
                "subtask4",
                "descriptionOfSubtask4",
                TaskStatus.NEW,
                epic2.getId()
        ));


        //проверка работоспособности
        System.out.println(taskManager.getAllTasks());
        task1.setStatus(TaskStatus.DONE);
        task1.setDescription("ohhhhh");
        taskManager.updateTask(task1);
        taskManager.updateTask(new Task(task2.getId(),
                "task2changed",
                "descripChanged",
                TaskStatus.DONE
        ));
        System.out.println(taskManager.getAllEpics());
        System.out.println(taskManager.getAllSubtasks());
        taskManager.deleteSubtaskById(subtask1.getId()); //удаление по id
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask2);
        taskManager.updateSubtask(new Subtask(subtask3.getId(),
                "titleChanged",
                "Changed",
                TaskStatus.DONE,
                epic1.getId()
        ));
        taskManager.updateEpic(epic1);
        epic2.setStatus(TaskStatus.DONE);// статус не должен обновиться
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
        taskManager.getSubtaskById(subtask2.getId());//должна удалиться вместе с эпиком
        task1.setTitle("last change");
        taskManager.updateTask(task1);
        taskManager.getTaskById(task1.getId());

        printAllTasks(taskManager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println();
        System.out.println("Эпики:");
        for (Task epic : manager.getAllEpics()) {
            System.out.println(epic);

            for (Task task : manager.getSubtasksOfEpic(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println();
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }
        System.out.println();
        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
