package server;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import util.InMemoryTaskManager;
import util.Managers;
import util.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryHandlerTest {
    private TaskManager taskManager;
    private HttpTaskServer server;
    private HttpClient client;
    private Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        gson = Managers.getGson();
        server = new HttpTaskServer(taskManager);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    private HttpResponse<String> sendGetHistoryRequest() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void testGetHistory_shouldReturn200AndEmptyList() throws Exception {
        HttpResponse<String> response = sendGetHistoryRequest();

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());

        List<?> history = gson.fromJson(response.body(), List.class);
        assertTrue(history.isEmpty());
    }

    @Test
    void testGetHistory_shouldReturnTasksInCorrectOrder() throws Exception {
        // тестовые задачи
        Task task = taskManager.createTask(new Task("Task 1", "Description"));
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Description"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask 1", "Description", epic.getId()));

        taskManager.getTaskById(task.getId());
        taskManager.getSubtaskById(subtask.getId());
        taskManager.getEpicById(epic.getId());

        HttpResponse<String> response = sendGetHistoryRequest();

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());

        List<?> history = gson.fromJson(response.body(), List.class);
        assertEquals(3, history.size());
    }

    @Test
    void testGetHistoryWithWrongMethod_shouldReturn405() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
        assertTrue(response.body().contains("Метод не поддерживается"));
    }

    @Test
    void testGetHistoryWithWrongPath_shouldReturn404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history/wrong"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Доступен только путь /history"));
    }

    @Test
    void testGetHistoryAfterTaskRemoval_shouldNotContainRemovedTask() throws Exception {
        Task task = taskManager.createTask(new Task("Task", "Description"));
        taskManager.getTaskById(task.getId());
        taskManager.deleteTaskById(task.getId());

        HttpResponse<String> response = sendGetHistoryRequest();

        assertEquals(200, response.statusCode());

        List<?> history = gson.fromJson(response.body(), List.class);
        assertTrue(history.isEmpty());
    }
}
