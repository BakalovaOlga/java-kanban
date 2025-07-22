package util;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;


public interface TaskManager {


    //Создание задач
    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    //Получение списков задач

    ArrayList<Task> getAllTasks();

    ArrayList<Subtask> getAllSubtasks();

    ArrayList<Epic> getAllEpics();

    ArrayList<Subtask> getSubtasksOfEpic(int epicId);

    List<Task> getHistory();

    // Расчет статуса эпика
    void updateEpicStatus(Epic epic);

    // Обновление задач
    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    //удаление задач

    void clearAll();

    void clearAllTasks();

    void clearAllEpics();

    void deleteAllSubtasks();

    void deleteTaskById(int id);

    void deleteSubtaskById(int id);

    void deleteEpicById(int id);

    Task getTaskById(int id);

    Subtask getSubtaskById(int id);

    Epic getEpicById(int id);

    TreeSet<Task> getPrioritizedTasks();


}




