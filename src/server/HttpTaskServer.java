package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import util.TaskManager;
import util.Managers;
import server.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    public static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        if (taskManager == null) {
            throw new IllegalArgumentException("TaskManager cannot be null");
        }
        this.taskManager = taskManager;
        this.gson = Managers.getGson();
        this.server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        this.setContext();
    }


    private void setContext() {
        // Создаем отдельные обработчики для каждого эндпоинта
        server.createContext("/tasks", new TaskHandler(taskManager, gson));
        server.createContext("/subtasks", new SubtaskHandler(taskManager, gson));
        server.createContext("/epics", new EpicHandler(taskManager, gson));
        server.createContext("/history", new HistoryHandler(taskManager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));
    }

    public Gson getGson() {
        return gson;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    public static void main(String[] args) {
        try {
            TaskManager taskManager = Managers.getDefault();
            HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager);
            httpTaskServer.start();

        } catch (IOException e) {
            System.err.println("Ошибка при запуске сервера: " + e.getMessage());

        }
    }
}