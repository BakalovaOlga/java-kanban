package server;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import util.InMemoryTaskManager;
import util.Managers;
import util.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskHandlerTest {
    protected static final int PORT = 8080;
    protected TaskManager taskManager;
    protected HttpTaskServer server;
    protected HttpClient client;
    protected Gson gson;


    @BeforeEach
    void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        server = new HttpTaskServer(taskManager);
        server.start();
        client = HttpClient.newHttpClient();
        gson = Managers.getGson();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    private HttpResponse<String> createEpic(Epic epic) throws Exception {
        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> createSubtask(Subtask subtask) throws Exception {
        String json = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void testCreateSubtask_shouldReturn201() throws Exception {
        // Создаем эпик сначала
        Epic epic = new Epic("Epic for subtask", "Description");
        HttpResponse<String> epicResponse = createEpic(epic);
        assertEquals(201, epicResponse.statusCode());

        // Создаем подзадачу
        Subtask subtask = new Subtask("Subtask", "Description", 1);
        HttpResponse<String> response = createSubtask(subtask);

        assertEquals(201, response.statusCode());
        Subtask createdSubtask = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(createdSubtask.getId());
        assertEquals("Subtask", createdSubtask.getTitle());
        assertEquals(1, createdSubtask.getEpicId());
    }

    @Test
    void testCreateSubtaskWithoutEpic_shouldReturn400() throws Exception {
        Subtask subtask = new Subtask("Subtask", "Description", 999); // Несуществующий эпик
        HttpResponse<String> response = createSubtask(subtask);

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Не указан или не существует эпик"));
    }

    @Test
    void testGetSubtaskById_shouldReturn200() throws Exception {
        // Создаем эпик и подзадачу
        createEpic(new Epic("Epic", "Description"));
        Subtask subtask = new Subtask("Test subtask", "Description", 1);
        HttpResponse<String> createResponse = createSubtask(subtask);
        Subtask createdSubtask = gson.fromJson(createResponse.body(), Subtask.class);

        // Получаем подзадачу
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + createdSubtask.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask retrievedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(createdSubtask.getId(), retrievedSubtask.getId());
        assertEquals("Test subtask", retrievedSubtask.getTitle());
    }

    @Test
    void testGetAllSubtasks_shouldReturn200() throws Exception {
        // Создаем эпик и подзадачи
        createEpic(new Epic("Epic", "Description"));
        createSubtask(new Subtask("Subtask 1", "Description", 1));
        createSubtask(new Subtask("Subtask 2", "Description", 1));

        // Получаем все подзадачи
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);
    }

    @Test
    void testDeleteSubtask_shouldReturn200() throws Exception {
        // Создаем эпик и подзадачу
        createEpic(new Epic("Epic", "Description"));
        Subtask subtask = new Subtask("To delete", "Description", 1);
        HttpResponse<String> createResponse = createSubtask(subtask);
        Subtask createdSubtask = gson.fromJson(createResponse.body(), Subtask.class);

        // Удаляем подзадачу
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + createdSubtask.getId()))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, deleteResponse.statusCode());
    }

    @Test
    void testCreateSubtaskWithTimeConflict_shouldReturn406() throws Exception {
        // Создаем эпик
        createEpic(new Epic("Epic", "Description"));

        // Первая подзадача с временем
        Subtask subtask1 = new Subtask("Subtask 1", "Description", 1);
        LocalDateTime now = LocalDateTime.now().withNano(0);
        subtask1.setStartTime(now);
        subtask1.setDuration(Duration.ofHours(1));
        createSubtask(subtask1);

        // Вторая подзадача с пересекающимся временем
        Subtask subtask2 = new Subtask("Subtask 2", "Description", 1);
        subtask2.setStartTime(now.plusMinutes(30));
        subtask2.setDuration(Duration.ofHours(1));
        HttpResponse<String> response = createSubtask(subtask2);

        assertEquals(406, response.statusCode());
    }
}
