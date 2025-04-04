package tasks;

import org.junit.jupiter.api.BeforeAll;
import  org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    private static Epic epic;
    private static Subtask subtask1;
    private static Subtask subtask2;

    @BeforeAll
    static void init() {
        subtask1 = new Subtask( "Subtask-1","some text");
        subtask2 = new Subtask( "Subtask-2", "some text 2");
    }

    @Test
    void mustBeEqualsById() {
        subtask2.setId(subtask1.getId());
        assertEquals(subtask1, subtask2, "Tasks are not equal");
    }


}