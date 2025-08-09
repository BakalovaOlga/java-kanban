package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
import util.InMemoryTaskManager;
import util.Managers;
import util.TaskManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrioritizedHandlerTest {
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

    private HttpResponse<String> sendGetPrioritizedRequest() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void testGetPrioritizedTasks_shouldReturn200AndEmptyList() throws Exception {
        HttpResponse<String> response = sendGetPrioritizedRequest();

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());

        List<?> tasks = gson.fromJson(response.body(), List.class);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void testGetPrioritizedTasks_shouldReturnTasksInCorrectOrder() throws Exception {
        // Создаем задачи с разным временем начала
        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(LocalDateTime.now().plusHours(2));
        taskManager.createTask(task1);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(LocalDateTime.now().plusHours(1));
        taskManager.createTask(task2);

        HttpResponse<String> response = sendGetPrioritizedRequest();

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());

        // Используем TypeToken для правильной десериализации
        Type taskListType = new TypeToken<List<Task>>() {
        }.getType();
        List<Task> tasks = gson.fromJson(response.body(), taskListType);

        assertEquals(2, tasks.size());

        // Проверяем порядок (первой должна быть task2, так как она раньше)
        assertEquals("Task 2", tasks.get(0).getTitle());
        assertEquals("Task 1", tasks.get(1).getTitle());
    }

    @Test
    void testGetPrioritizedTasksWithMixedTimes() throws Exception {
        // Создаем задачи без времени
        taskManager.createTask(new Task("Task 1", "Description"));
        taskManager.createTask(new Task("Task 2", "Description"));
        // И со временем
        Task task3 = new Task("Task 3", "Description");
        task3.setStartTime(LocalDateTime.now().withNano(0));
        task3.setDuration(Duration.ofMinutes(60));
        taskManager.createTask(task3);


        HttpResponse<String> response = sendGetPrioritizedRequest();

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());

        List<?> tasks = gson.fromJson(response.body(), List.class);
        assertEquals(1, tasks.size()); // в список попадают только задачи со временем
    }

    @Test
    void testGetPrioritizedWithWrongMethod_shouldReturn405() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
        assertTrue(response.body().contains("Метод не поддерживается"));
    }

    @Test
    void testGetPrioritizedWithWrongPath_shouldReturn404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized/wrong"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Доступен только путь /prioritized"));
    }
}
