package util;

import java.util.ArrayList;
import java.util.HashMap;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

public class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private int newId = 1;

    //Создание задач
    public Task createTask(Task task) {
        task.setId(newId++);
        tasks.put(task.getId(), task);
        return task;
    }

    public Subtask createSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Эпик с id " + subtask.getEpicId() + " не существует.");
        }
        subtask.setId(newId++);
        subtasks.put(subtask.getId(), subtask);
        //добавить подзадачу в список эпика и обновить его статус
        Epic epic = epics.get(subtask.getEpicId());
        epic.getSubtaskId().add(subtask.getId());
        updateEpicStatus(epic);
        return subtask;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(newId++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    //Получение списков задач

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getSubtasksOfEpic(int epicId) {
      Epic epic = epics.get(epicId);//смотрим есть ли эпик с таким id
      if (epic == null) { // если нет
          return null;
      }

      ArrayList<Subtask> subtaskList = new ArrayList<>(); //новый список в который сохранятся подзадачи если они есть
        for (int subtaskId : epic.getSubtaskId()) {//проходим по списку подзадач эпика
            Subtask subtask = subtasks.get(subtaskId);//ищем подзадачу
            if (subtask != null) {//если есть, сохраняем в список
                subtaskList.add(subtask);
            }
        }
        return subtaskList; // возвращаем список
    }

    // Расчет статуса эпика
    private void updateEpicStatus(Epic epic) {
        ArrayList<TaskStatus> statuses = new ArrayList<>();
        boolean allDone = true;
        boolean allNew = true;

        for (Integer subtaskId : epic.getSubtaskId()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                TaskStatus status = subtask.getStatus();

                if (status != TaskStatus.DONE) {
                    allDone = false;
                }
                if (status != TaskStatus.NEW) {
                    allNew = false;
                }
            }
        }


        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    // Обновление задач
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId()))
            return;
        Task current = tasks.get(task.getId());
        current.setTitle(task.getTitle());
        current.setDescription(task.getDescription());
        current.setStatus(task.getStatus());
        tasks.put(task.getId(), current);
    }

    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) {
            return;
        }

        Subtask current = subtasks.get(subtask.getId());
        int oldEpicId = current.getEpicId();
        int newEpicId = subtask.getEpicId();

        if (oldEpicId != newEpicId) {

            Epic oldEpic = epics.get(oldEpicId);
            if (oldEpic != null) {
                oldEpic.getSubtaskId().remove((Integer) subtask.getId());
                updateEpicStatus(oldEpic);
            }

            Epic newEpic = epics.get(newEpicId);
            if (newEpic != null) {
                newEpic.getSubtaskId().add(subtask.getId());
                updateEpicStatus(newEpic);
            }
        }

        current.setTitle(subtask.getTitle());
        current.setDescription(subtask.getDescription());
        current.setStatus(subtask.getStatus());
        current.setEpicId(newEpicId);
        subtasks.put(subtask.getId(), current);

        Epic epic = epics.get(newEpicId);
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) return;

        Epic current = epics.get(epic.getId());
        current.setTitle(epic.getTitle());
        current.setDescription(epic.getDescription());
        epics.put(epic.getId(), current);

        updateEpicStatus(current);
    }

    //удаление задач

    public void clearAll(){
        tasks.clear();
        subtasks.clear();
        epics.clear();
    }

    public void clearAllTasks(){
        tasks.clear();
    }

    public void clearAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskId().clear();
            updateEpicStatus(epic);
        }
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskId().remove(id);
                updateEpicStatus(epic);
            }
        }
    }
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            ArrayList<Integer> subtaskIds = epic.getSubtaskId();
            for (Integer subtaskId : subtaskIds) {
                subtasks.remove(subtaskId);
            }
        }
    }
    public Task getTaskById(int id) {
        return (Task) tasks.get(id);
    }
    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public Epic getEpicById(int id) {
        return (Epic) epics.get(id);
    }
}




