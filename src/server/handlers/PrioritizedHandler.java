package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import util.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {
    private final Gson gson;

    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        super(taskManager);
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();

            if (!path.equals("/prioritized")) {
                sendNotFound(exchange, "Доступен только путь /prioritized");
                return;
            }

            if (!exchange.getRequestMethod().equals("GET")) {
                sendText(exchange,
                        "{\"error\":\"Метод не поддерживается. Используйте GET\"}",
                        405);
                return;
            }

            sendText(exchange, gson.toJson(taskManager.getPrioritizedTasks()), 200);

        } catch (Exception e) {
            sendInternalError(exchange, "Ошибка при получении приоритизированных задач");
        }
    }
}
