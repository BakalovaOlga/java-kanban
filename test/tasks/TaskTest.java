package tasks;

import org.junit.jupiter.api.BeforeAll;
import  org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    private static Task task1;
    private static Task task2;

    @BeforeAll
    static void init() {
        task1 = new Task( "Task-1","some text");
        task2 = new Task( "Task-2", "some text 2");
    }

    @Test
    void mustBeEqualsById() {
        task2.setId(task1.getId());
        assertEquals(task1, task2, "Tasks are not equal");
    }

}