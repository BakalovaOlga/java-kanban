package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import util.Managers;
import util.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected final TaskManager taskManager;
    protected final Gson gson;

    /**
     * Создает обработчик с указанным менеджером задач.
     *
     * @param taskManager Менеджер задач для операций с данными.
     */
    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = Managers.getGson();
    }

    @Override
    public abstract void handle(HttpExchange exchange) throws IOException;

    /**
     * Базовый метод для отправки текстового ответа
     */
    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    /**
     * Успешный ответ с данными (200 OK)
     */
    protected void sendSuccess(HttpExchange exchange, String responseData) throws IOException {
        sendText(exchange, responseData, 200);
    }

    /**
     * Успешное создание без возврата данных (201 Created)
     */
    protected void sendCreated(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(201, -1);
        exchange.close();
    }

    /**
     * Некорректный запрос (400 Bad Request)
     */
    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        String json = gson.toJson(Map.of("error", message));
        sendText(exchange, json, 400);
    }

    /**
     * Ресурс не найден (404 Not Found)
     */
    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        String json = gson.toJson(Map.of("error", message));
        sendText(exchange, json, 404);
    }

    /**
     * Конфликт/пересечение задач (406 Not Acceptable)
     */
    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        String json = gson.toJson(Map.of("error", "Задачи пересекаются по времени"));
        sendText(exchange, json, 406);
    }

    /**
     * Внутренняя ошибка сервера (500 Internal Server Error)
     */
    protected void sendInternalError(HttpExchange exchange, String message) throws IOException {
        String json = gson.toJson(Map.of("error", message));
        sendText(exchange, json, 500);
    }

    /**
     * Возвращает id из пути
     */
    protected int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[2]);
    }
}
