package util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import exceptions.TimeConflictException;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private int newId = 0;
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    protected final TreeSet<Task> prioritizedTasks =
            new TreeSet<>(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));

    private int generateID() {
        return ++newId;
    }

    protected void updateMaxId(int existingId) {
        if (existingId >= newId) {
            newId = existingId + 1;
        }
    }

    /**
     * Методы создания задач, подзадач и эпиков
     */
    @Override
    public Task createTask(Task task) {
        if (isTasksOverlap(task)) {
            throw new TimeConflictException("Задача пересекается по времени с существующей");
        }
        if (task.getId() != 0) {
            tasks.put(task.getId(), task);
            updateMaxId(task.getId());
            return task;
        }
        task.setId(generateID());
        tasks.put(task.getId(), task);
        prioritizedCheck(task);
        return task;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (isTasksOverlap(subtask)) {
            throw new TimeConflictException("Подзадача пересекается по времени с существующей");
        }
        if (subtask.getId() != 0) {
            subtasks.put(subtask.getId(), subtask);
            updateMaxId(subtask.getId());
            return subtask;
        }
        subtask.setId(generateID());
        subtasks.put(subtask.getId(), subtask);
        //добавить подзадачу в список эпика и обновить его статус
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.getSubtaskId().add(subtask.getId());
            updateEpicStatus(epic);
            setEpicStartEndTimeAndDuration(epic);
        }
        prioritizedCheck(subtask);
        return subtask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        if (epic.getId() != 0) {
            epics.put(epic.getId(), epic);
            updateMaxId(epic.getId());
            return epic;
        }
        setEpicStartEndTimeAndDuration(epic);
        epic.setId(generateID());
        epics.put(epic.getId(), epic);
        prioritizedCheck(epic);
        return epic;
    }

    //Получение списков задач

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return null;
        }

        return epic.getSubtaskId().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // Расчет статуса эпика
    @Override
    public void updateEpicStatus(Epic epic) {
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

    //Расчет времени эпика
    private void setEpicStartEndTimeAndDuration(Epic epic) {
        if (getSubtasksOfEpic(epic.getId()) == null || getSubtasksOfEpic(epic.getId()).isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(null);
            epic.setEndTime(null);
            return;
        }

        // Определение самого раннего startTime
        LocalDateTime earliestStart = getSubtasksOfEpic(epic.getId()).stream() // Создание потока из списка подзадач
                .map(Subtask::getStartTime) // Для каждой подзадачи получение ее startTime (преобразование в startTime)
                .filter(Objects::nonNull) // Фильтрация null значений
                .min(LocalDateTime::compareTo) // Поиск самого раннего времени
                .orElse(null); // Если все startTime = null, возвращается null

        // Определение самого позднего endTime
        LocalDateTime latestEnd = getSubtasksOfEpic(epic.getId()).stream() // Создание потока из списка подзадач
                .map(Subtask::getEndTime) // Для каждой подзадачи вычисление ее endTime (преобразование в endTime)
                .filter(Objects::nonNull) // Фильтрация null значений
                .max(LocalDateTime::compareTo) // Поиск самого позднего времени
                .orElse(null); // Если все endTime = null, возвращается null

        epic.setStartTime(earliestStart); // Старт epic = старт самой ранней подзадачи
        epic.setEndTime(latestEnd); // Окончание epic = окончание самой поздней подзадачи
        if (earliestStart != null && latestEnd != null) {
            epic.setDuration(Duration.between(earliestStart, latestEnd)); // Длительность = разница времен
        } else {
            epic.setDuration(null);
        }
    }

    // Обновление задач
    @Override
    public void updateTask(Task task) {
        if (isTasksOverlap(task)) {
            throw new TimeConflictException("Задача пересекается по времени с существующей");
        }
        if (task == null || !tasks.containsKey(task.getId()))
            return;
        Task current = tasks.get(task.getId());
        current.setTitle(task.getTitle());
        current.setDescription(task.getDescription());
        current.setStatus(task.getStatus());
        tasks.put(task.getId(), current);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask old = subtasks.get(subtask.getId());
        if (old != null && !old.equals(subtask)) {
            if (isTasksOverlap(subtask)) { // Проверка только если задача реально изменилась
                throw new TimeConflictException("Подзадача пересекается по времени с существующей");
            }
        }
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

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) return;

        Epic current = epics.get(epic.getId());
        current.setTitle(epic.getTitle());
        current.setDescription(epic.getDescription());
        epics.put(epic.getId(), current);

        updateEpicStatus(current);
    }

    //удаление задач

    @Override
    public void clearAll() {
        prioritizedTasks.clear();
        Stream.of(tasks.keySet(), epics.keySet(), subtasks.keySet())
                .flatMap(Set::stream)
                .forEach(historyManager::remove);
        tasks.clear();
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void clearAllTasks() {
        tasks.keySet().forEach(id -> prioritizedTasks.removeIf(t -> t.getId() == id));
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
    }

    @Override
    public void clearAllEpics() {
        subtasks.keySet().forEach(id -> prioritizedTasks.removeIf(t -> t.getId() == id));
        Stream.of(tasks.keySet(), epics.keySet(), subtasks.keySet())
                .flatMap(Set::stream)
                .forEach(historyManager::remove);
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.keySet().forEach(id -> prioritizedTasks.removeIf(t -> t.getId() == id));
        // Удаление подзадач из historyManager и очистка мапы
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();

        // Очистка подзадач эпиков и обновление статусов
        epics.values().stream()
                .peek(epic -> epic.getSubtaskId().clear())
                .forEach(this::updateEpicStatus);
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.removeIf(t -> t.getId() == id);
        }
        historyManager.remove(id);
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.removeIf(t -> t.getId() == id);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskId().remove(Integer.valueOf(id));
                updateEpicStatus(epic);
            }
        }
        historyManager.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) return;

        epic.getSubtaskId().forEach(subtaskId -> {
            prioritizedTasks.removeIf(t -> t.getId() == subtaskId);
            historyManager.remove(subtaskId);
            subtasks.remove(subtaskId);
        });

        historyManager.remove(id);
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void prioritizedCheck(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(Task.copyTask(task));
        }
    }

    public TreeSet<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    public boolean isTasksOverlap(Task task) {
        if (task.getStartTime() == null || task.getEndTime() == null) {
            return false;
        }
        return prioritizedTasks.stream()
                .filter(current -> current.getId() != task.getId())  // Вот эта `t`
                .anyMatch(current -> !task.getStartTime().isAfter(current.getEndTime())
                        && !current.getStartTime().isAfter(task.getEndTime()));
    }

}
