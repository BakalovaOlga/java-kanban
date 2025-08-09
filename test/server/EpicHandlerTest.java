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

import static org.junit.jupiter.api.Assertions.*;

public class EpicHandlerTest {
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

    private HttpResponse<String> sendCreateEpicRequest(Epic epic) throws Exception {
        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendGetRequest(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendDeleteRequest(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void testCreateEpic_shouldReturn201AndCreatedEpic() throws Exception {
        Epic epic = new Epic("Test Epic", "Description");
        HttpResponse<String> response = sendCreateEpicRequest(epic);

        assertEquals(201, response.statusCode());
        assertNotNull(response.body());

        Epic createdEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(createdEpic);
        assertTrue(createdEpic.getId() > 0);
        assertEquals("Test Epic", createdEpic.getTitle());
    }

    @Test
    void testGetEpicById_shouldReturn200AndEpic() throws Exception {
        Epic epic = new Epic("Test Epic", "Description");
        Epic createdEpic = taskManager.createEpic(epic);

        HttpResponse<String> response = sendGetRequest("/epics/" + createdEpic.getId());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());

        Epic retrievedEpic = gson.fromJson(response.body(), Epic.class);
        assertEquals(createdEpic.getId(), retrievedEpic.getId());
        assertEquals("Test Epic", retrievedEpic.getTitle());
    }

    @Test
    void testGetEpicSubtasks_shouldReturn200AndSubtasksList() throws Exception {
        Epic epic = taskManager.createEpic(new Epic("Test Epic", "Description"));
        taskManager.createSubtask(new Subtask("Subtask 1", "Desc 1", epic.getId()));
        taskManager.createSubtask(new Subtask("Subtask 2", "Desc 2", epic.getId()));

        HttpResponse<String> response = sendGetRequest("/epics/" + epic.getId() + "/subtasks");

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);
    }

    @Test
    void testDeleteEpic_shouldReturn200() throws Exception {
        Epic epic = taskManager.createEpic(new Epic("To Delete", "Description"));

        HttpResponse<String> response = sendDeleteRequest("/epics/" + epic.getId());

        assertEquals(200, response.statusCode());
        assertNull(taskManager.getEpicById(epic.getId()));
    }

    @Test
    void testCreateEpicWithInvalidData_shouldReturn400() throws Exception {
        String invalidJson = "{invalid json}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Неверный формат JSON"));
    }

}
