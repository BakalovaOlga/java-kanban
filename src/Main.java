import util.TaskManager;
import tasks.*;
public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        //создаем задачи

        Task task1 = taskManager.createTask(new Task(0,
                "Прочитать ТЗ к заданию",
                "Не плакать",
                TaskStatus.NEW
        ));
        Task task2 = taskManager.createTask(new Task(0,
                "Составить алгоритм",
                "Подумать ещё раз",
                TaskStatus.NEW
        ));

        Epic epic1 = taskManager.createEpic(new Epic(0, "Сделать задание", "до 24 марта"));
        Epic epic2 = taskManager.createEpic(new Epic(0, "Отдыхать", "2 дня"));


        Subtask subtask1 = taskManager.createSubtask(new Subtask(0,
                "Нарисовать блок-схему",
                "Для лучшего понимания",
                TaskStatus.NEW,
                epic1.getId()
        ));
        Subtask subtask2 = taskManager.createSubtask(new Subtask(0,
                "Создать основу программы",
                "начать делать наброски кода.",
                TaskStatus.NEW,
                epic1.getId()
        ));
        Subtask subtask3 = taskManager.createSubtask(new Subtask(0,
                "Сделать всё по ТЗ",
                "Проверить, точно ли всё. Возможно, что-то переделать.",
                TaskStatus.NEW,
                epic1.getId()
        ));
        Subtask subtask4 = taskManager.createSubtask(new Subtask(0,
                "Протестировать работу программы",
                "Найти и исправить критические ошибки.",
                TaskStatus.NEW,
                epic1.getId()
        ));
        Subtask subtask5 = taskManager.createSubtask(new Subtask(0,
                "Отправить на проверку",
                "Надеяться, что с нуля переделывать не придётся)",
                TaskStatus.NEW,
                epic1.getId()
        ));
        Subtask subtask6 = taskManager.createSubtask(new Subtask(0,
                "Отправить на проверку",
                "Надеяться, что с нуля переделывать не придётся)",
                TaskStatus.NEW,
                epic1.getId()
        ));
        Subtask subtask7 = taskManager.createSubtask(new Subtask(0,
                "Провести время с сыном",
                "Сходить в детский центр",
                TaskStatus.NEW,
                epic2.getId()
        ));

        //проверка работоспособности
        System.out.println(taskManager.getAllTasks());
        task1.setStatus(TaskStatus.DONE);
        task1.setDescription("Поплакать");
        taskManager.updateTask(task1);
        taskManager.updateTask(new Task(task2.getId(),
                "Подумать над алгоритмизацией задания",
                "Два раза",
                TaskStatus.DONE
        ));
        System.out.println(taskManager.getAllTasks());
        System.out.println();
        System.out.println(taskManager.getAllEpics());
        System.out.println(taskManager.getAllSubtasks());
        taskManager.deleteSubtaskById(subtask1.getId()); //удаление по id
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);
        taskManager.updateSubtask(new Subtask(subtask3.getId(),
                "сделать всё по ТЗ",
                "Точно всё",
                TaskStatus.DONE,
                epic1.getId()
        ));
        subtask4.setTitle("Проверить работоспособность");
        subtask4.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask4);
        subtask5.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask5);
        taskManager.updateSubtask(new Subtask(subtask6.getId(),
                "Сходить погулять",
                "В парк",
                TaskStatus.IN_PROGRESS,
                epic2.getId()
        ));// переименование и перемещение в другой эпик
        taskManager.updateEpic(epic1);
        epic2.setStatus(TaskStatus.DONE);// статус не должен обновиться
        epic2.setDescription("3 дня");
        taskManager.updateEpic(epic2);
        System.out.println();
        System.out.println("Обновленный список задач");
        System.out.println(taskManager.getAllEpics());
        System.out.println(taskManager.getAllSubtasks());
        System.out.println();
        System.out.println("Подзадачи epic2");
        System.out.println(taskManager.getSubtasksOfEpic(epic2.getId()));
        System.out.println();
        System.out.println("получаем задачу по id");
        System.out.println(taskManager.getTaskById(task2.getId()));
    }
}
