package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import util.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    private final Gson gson;

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(taskManager);
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();

            if (!path.equals("/history")) {
                sendNotFound(exchange, "Доступен только путь /history");
                return;
            }
            if (!exchange.getRequestMethod().equals("GET")) {
                sendText(exchange, "{\"error\":\"Метод не поддерживается\"}", 405);
                return;
            }
            sendText(exchange, gson.toJson(taskManager.getHistory()), 200);
        } catch (Exception e) {
            sendInternalError(exchange, "Ошибка при получении истории задач");
        }
    }
}
