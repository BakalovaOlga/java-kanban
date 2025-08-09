package server.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import exceptions.TimeConflictException;
import tasks.Subtask;
import util.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubtaskHandler extends BaseHttpHandler {

    public SubtaskHandler(TaskManager taskManager) {
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
        if (path.equals("/subtasks")) {
            sendText(exchange, gson.toJson(taskManager.getAllSubtasks()), 200);
        } else if (path.matches("/subtasks/\\d+")) {
            try {
                int id = extractIdFromPath(path);
                Subtask subtask = taskManager.getSubtaskById(id);
                if (subtask == null) {
                    sendNotFound(exchange, "Подзадача не найдена.");
                } else {
                    sendText(exchange, gson.toJson(subtask), 200);
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "ID подзадачи должен быть числом");
            }
        } else {
            sendNotFound(exchange, "Неверный путь запроса");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String json = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Subtask subtask = gson.fromJson(json, Subtask.class);

        if (subtask == null) {
            sendBadRequest(exchange, "Тело запроса должно содержать валидную подзадачу");
            return;
        }

        if (subtask.getTitle() == null || subtask.getDescription() == null) {
            sendBadRequest(exchange, "Поля title и description должны быть инициализированы");
            return;
        }
        if (subtask.getEpicId() == 0 || taskManager.getEpicById(subtask.getEpicId()) == null) {
            sendBadRequest(exchange, "Не указан или не существует эпик для подзадачи");
            return;
        }

        if (subtask.getStartTime() != null && subtask.getEndTime() != null &&
                subtask.getStartTime().isAfter(subtask.getEndTime())) {
            sendBadRequest(exchange, "Дата начала не может быть позже даты окончания");
            return;
        }

        try {
            if (subtask.getId() == 0 || taskManager.getSubtaskById(subtask.getId()) == null) {

                sendText(exchange, gson.toJson(taskManager.createSubtask(subtask)), 201);
            } else {
                sendText(exchange, gson.toJson(taskManager.updateSubtask(subtask)), 200);
            }

        } catch (TimeConflictException e) {
            sendHasInteractions(exchange);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Неверный формат JSON");
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/subtasks")) {
            taskManager.deleteAllSubtasks();
            sendSuccess(exchange, "Все подзадачи удалены.");
        } else if (path.matches("/subtasks/\\d+")) {
            try {
                int id = extractIdFromPath(path);
                Subtask subtask = taskManager.getSubtaskById(id);
                if (subtask == null) {
                    sendNotFound(exchange, "Подзадача не найдена.");
                } else {
                    taskManager.deleteSubtaskById(id);
                    sendSuccess(exchange, "Подзадача удалена.");
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "ID подзадачи должен быть числом.");
            }
        } else {
            sendNotFound(exchange, "Неверный путь запроса");
        }
    }
}
