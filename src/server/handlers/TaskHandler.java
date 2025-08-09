package server.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tasks.Task;
import util.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TaskHandler extends BaseHttpHandler {
    public TaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET" -> handleGet(exchange);
                case "POST" -> handlePost(exchange);
                case "DELETE" -> handleDelete(exchange);
                default -> sendText(exchange, "{\"error\":\"Метод не поддерживается\"}", 405);
            }
        } catch (Exception e) {
            sendInternalError(exchange, e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/tasks")) {
            sendText(exchange, gson.toJson(taskManager.getAllTasks()), 200);
        } else if (path.matches("/tasks/\\d+")) {
            try {
                int id = extractIdFromPath(path);
                Task task = taskManager.getTaskById(id);
                if (task == null) {
                    sendNotFound(exchange, "Задача не найдена.");
                } else {
                    sendText(exchange, gson.toJson(task), 200);
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "ID задачи должен быть числом.");
            }
        } else {
            sendNotFound(exchange, "Неверный путь запроса");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String json = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            Task task = gson.fromJson(json, Task.class);

            if (task == null) {
                sendBadRequest(exchange, "Тело запроса должно содержать валидную задачу");
                return;
            }

            if (task.getTitle() == null || task.getTitle().isBlank() ||
                    task.getDescription() == null || task.getDescription().isBlank()) {
                sendBadRequest(exchange, "Поля title и description должны быть заполнены.");
                return;
            }

            if (task.getStartTime() != null && task.getEndTime() != null &&
                    task.getStartTime().isAfter(task.getEndTime())) {
                sendBadRequest(exchange, "Дата начала не может быть позже даты окончания");
                return;
            }

            if (taskManager.isTasksOverlap(task)) {
                sendHasInteractions(exchange);
                return;
            }

            Task resultTask;
            if (task.getId() == 0 || taskManager.getTaskById(task.getId()) == null) {
                resultTask = taskManager.createTask(task);
                sendText(exchange, gson.toJson(resultTask), 201);
            } else {
                resultTask = taskManager.updateTask(task);
                sendText(exchange, gson.toJson(resultTask), 200);
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Неверный формат JSON");
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/tasks")) {
            taskManager.clearAllTasks();
            sendSuccess(exchange, "Все задачи удалены.");
        } else if (path.matches("/tasks/\\d+")) {
            try {
                int id = extractIdFromPath(path);
                Task task = taskManager.getTaskById(id);
                if (task == null) {
                    sendNotFound(exchange, "Задача не найдена");
                } else {
                    taskManager.deleteTaskById(id);
                    sendSuccess(exchange, "Задача удалена");
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "ID задачи должен быть числом");
            }
        } else {
            sendNotFound(exchange, "Неверный путь запроса");
        }
    }
}
