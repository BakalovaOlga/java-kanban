package server.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tasks.Epic;
import util.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EpicHandler extends BaseHttpHandler {

    public EpicHandler(TaskManager taskManager) {
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
        if (path.equals("/epics")) {
            sendText(exchange, gson.toJson(taskManager.getAllEpics()), 200);
        } else if (path.matches("/epics/\\d+")) {
            handleGetEpicById(exchange, path);
        } else if (path.matches("/epics/\\d+/subtasks")) {
            handleGetEpicSubtasks(exchange, path);
        } else {
            sendNotFound(exchange, "Неверный путь запроса");
        }
    }

    private void handleGetEpicById(HttpExchange exchange, String path) throws IOException {
        try {
            int id = extractIdFromPath(path);
            Epic epic = taskManager.getEpicById(id);
            if (epic == null) {
                sendNotFound(exchange, "Эпик не найден.");
            } else {
                sendText(exchange, gson.toJson(epic), 200);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "ID эпика должен быть числом.");
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange, String path) throws IOException {
        try {
            int id = extractIdFromPath(path);
            sendText(exchange, gson.toJson(taskManager.getSubtasksOfEpic(id)), 200);
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "ID эпика должен быть числом");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String json = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            Epic epic = gson.fromJson(json, Epic.class);

            if (epic == null) {
                sendBadRequest(exchange, "Тело запроса должно содержать валидный эпик");
                return;
            }
            if (epic.getTitle() == null || epic.getDescription() == null) {
                sendBadRequest(exchange, "Поля title и description должны быть инициализированы");
                return;
            }

            Epic resultEpic;
            if (epic.getId() == 0 || taskManager.getEpicById(epic.getId()) == null) {
                resultEpic = taskManager.createEpic(epic); // Получаем созданный эпик
                String responseBody = gson.toJson(resultEpic); // Сериализуем его
                sendText(exchange, responseBody, 201); // Отправляем в ответе
            } else {
                resultEpic = taskManager.updateEpic(epic);
                String responseBody = gson.toJson(resultEpic);
                sendText(exchange, responseBody, 200);
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Неверный формат JSON");
        }
    }

    /**
     * Обработка DELETE-запросов:
     * - DELETE /epics/{id} - удаляет эпик по ID
     * - DELETE /epics - удаляет все эпики
     */
    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/epics")) {
            taskManager.clearAllEpics();
            sendSuccess(exchange, "Все эпики удалены");
        } else if (path.matches("/epics/\\d+")) {
            try {
                int id = extractIdFromPath(path);
                Epic epic = taskManager.getEpicById(id);
                if (epic == null) {
                    sendNotFound(exchange, "Эпик не найден");
                } else {
                    taskManager.deleteEpicById(id);
                    sendSuccess(exchange, "Эпик удален");
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "ID эпика должен быть числом");
            }
        } else {
            sendNotFound(exchange, "Неверный путь запроса");
        }
    }
}
