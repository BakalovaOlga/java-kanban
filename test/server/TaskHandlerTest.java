package server;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
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

class TaskHandlerTest {
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

    private HttpResponse<String> createTask(Task task) throws Exception {
        String json = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void testGetAllTasks_emptyList_returns200() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void testCreateTaskWithTime_returns201() throws Exception {
        Task task = new Task("Task with time", "Description");
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));

        HttpResponse<String> response = createTask(task);

        assertEquals(201, response.statusCode());
        Task createdTask = gson.fromJson(response.body(), Task.class);
        assertNotNull(createdTask.getStartTime());
        assertNotNull(createdTask.getDuration());
        assertNotNull(createdTask.getEndTime());
    }

    @Test
    void testCreateTaskWithTimeOverlap_returns406() throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofHours(1);

        // Первая задача
        Task task1 = new Task("Task 1", "Description 1");
        task1.setStartTime(startTime);
        task1.setDuration(duration);
        createTask(task1);

        // Вторая задача с пересекающимся временем
        Task task2 = new Task("Task 2", "Description 2");
        task2.setStartTime(startTime.plusMinutes(30));
        task2.setDuration(duration);

        HttpResponse<String> response = createTask(task2);

        assertEquals(406, response.statusCode());
    }

    @Test
    void testUpdateTaskTime_returns200() throws Exception {
        // Фиксируем время с обнуленными наносекундами
        LocalDateTime originalTime = LocalDateTime.now().withNano(0);
        Duration originalDuration = Duration.ofHours(1);

        // Создаем задачу
        Task task = new Task("Original Task", "Description");
        task.setStartTime(originalTime);
        task.setDuration(originalDuration);
        HttpResponse<String> createResponse = createTask(task);
        Task createdTask = gson.fromJson(createResponse.body(), Task.class);

        // Переносим начало на день вперед
        LocalDateTime updatedTime = originalTime.plusDays(1).withNano(0);
        Duration updatedDuration = Duration.ofHours(2);

        // Обновляем время задачи
        createdTask.setStartTime(updatedTime);
        createdTask.setDuration(updatedDuration);
        String json = gson.toJson(createdTask);
        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, updateResponse.statusCode());
        Task updatedTask = gson.fromJson(updateResponse.body(), Task.class);

        // время в задаче должно измениться
        assertEquals(updatedTime, updatedTask.getStartTime());
        assertEquals(updatedDuration, updatedTask.getDuration());

        // новое время отличается от старого
        assertNotEquals(originalTime, updatedTask.getStartTime());
        assertNotEquals(originalDuration, updatedTask.getDuration());
    }

    @Test
    void testGetTaskWithTime_returnsCorrectEndTime() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().withNano(0);
        Duration duration = Duration.ofHours(2);

        Task task = new Task("Timed Task", "Description");
        task.setStartTime(startTime);
        task.setDuration(duration);
        HttpResponse<String> createResponse = createTask(task);
        Task createdTask = gson.fromJson(createResponse.body(), Task.class);

        // Получаем задачу
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + createdTask.getId()))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        Task retrievedTask = gson.fromJson(getResponse.body(), Task.class);

        assertEquals(200, getResponse.statusCode());
        assertEquals(startTime.plus(duration), retrievedTask.getEndTime());
    }
}