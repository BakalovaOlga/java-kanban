package util;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import tasks.*;
import exceptions.ManagerSaveException;

/**
 * Расширение InMemoryTaskManager, добавляет функционал сохранения состояния в CSV-файл.
 */
public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File savedFile;

    public FileBackedTaskManager(File savedFile) {
        this.savedFile = savedFile;
    }

    /**
     * Метод сохранения данных в файл.
     */
    private void save() {
        try (FileWriter writer = new FileWriter(savedFile)) {
            String header = "id,type,name,status,description,startTime,duration,epic\n";
            writer.write(header);
            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения данных.");
        }
    }

    /**
     * Метод загрузки данных из файла.
     *
     * @param file - файл с сохраненными данными
     * @return - новый экземпляр FileBackedTaskManager с загруженными данными
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        if (file == null || !file.exists()) {
            throw new ManagerSaveException("Файл не существует или недоступен для чтения");
        }
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();   //пропуск строки header'a;
            while (reader.ready()) {
                lines.add(reader.readLine());
            }
            for (String line : lines) {
                Task task = fromString(line);
                if (task != null) {
                    try {
                        switch (task.getType()) {
                            case EPIC:
                                manager.createEpic((Epic) task);
                                break;
                            case SUBTASK:
                                manager.createSubtask((Subtask) task);
                                break;
                            default:
                                manager.createTask(task);
                        }
                    } catch (IllegalArgumentException e) {
                        throw new ManagerSaveException("Ошибка при чтении файла: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении файла: " + e.getMessage());
        }
        for (Subtask subtask : manager.getAllSubtasks()) {
            Epic epic = manager.getEpicById(subtask.getEpicId());
            if (epic != null) {
                manager.addSubtaskToEpic(epic, subtask);
            } else {
                System.err.printf("Эпик %d для подзадачи %d не найден%n",
                        subtask.getEpicId(), subtask.getId());
            }
        }
        return manager;
    }

    /**
     * Преобразование задачи в строку CSV
     *
     * @param task Задача для преобразования
     * @return Строка в формате CSV
     */
    private String toString(Task task) {
        StringBuilder result = new StringBuilder()
                .append(task.getId()).append(",")
                .append(task.getType()).append(",")
                .append(task.getTitle()).append(",")
                .append(task.getStatus()).append(",")
                .append(task.getDescription()).append(",")
                .append(task.getStartTime() != null ? task.getStartTime() : "").append(",")
                .append(task.getDuration() != null ? task.getDuration().toMinutes() : "");

        if (task.getType() == TaskType.SUBTASK) {
            result.append(",").append(((Subtask) task).getEpicId());
        }

        return result.toString();
    }

    /**
     * Преобразование строки CSV в объект задачи
     *
     * @param value - строка с данными задачи
     * @return - объект задачи (Task, Epic или Subtask)
     */
    private static Task fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        String[] data = value.split(",");
        if (data.length < 7) {
            throw new IllegalArgumentException("Некорректная строка " + value);
        }
        try {
            int id = Integer.parseInt(data[0]);
            TaskType type = TaskType.valueOf(data[1]);
            String title = data[2];
            TaskStatus status = TaskStatus.valueOf(data[3]);
            String description = data[4];

            // Обработка времени начала и продолжительности
            LocalDateTime startTime = data[5].isEmpty() ? null : LocalDateTime.parse(data[5]);
            Duration duration = data[6].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(data[6]));

            switch (type) {
                case TASK:
                    return new Task(id, title, description, status, startTime, duration);
                case EPIC:
                    Epic epic = new Epic(id, title, description, status, startTime, duration);
                    return epic;
                case SUBTASK:
                    if (data.length < 8 || data[7].isEmpty()) {
                        throw new IllegalArgumentException("Для подзадачи не указан эпик: " + value);
                    }
                    int epicId = Integer.parseInt(data[7]);
                    return new Subtask(id, title, description, status, epicId, startTime, duration);
                default:
                    throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Ошибка парсинга строки: " + value, e);
        }
    }

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void clearAllTasks() {
        super.clearAllTasks();
        save();
    }

    @Override
    public void clearAllEpics() {
        super.clearAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void clearAll() {
        super.clearAll();
        save();
    }

    /**
     * Метод для восстановления связи эпик-подзадача, используется при загрузке из файла.
     */
    private void addSubtaskToEpic(Epic epic, Subtask subtask) {
        epic.getSubtaskId().add(subtask.getId());
        updateEpicStatus(epic);
    }
}
