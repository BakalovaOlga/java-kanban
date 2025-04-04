package tasks;
import  org.junit.jupiter.api.Test;
import  org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private static Epic epic1;
    private static Epic epic2;

    @BeforeAll
    static void init() {
        epic1 = new Epic( "Epic-1","some text");
        epic2 = new Epic( "Epic-2", "some text 2");
    }

    @Test
    void mustBeEqualsById() {
        epic2.setId(epic1.getId());
        assertEquals(epic1, epic2, "Epics are not equal");
    }

    @Test
    void canNotAddSelfAsSubtask() {
        Epic epic = new Epic("Epic", "Desc");
        epic.setId(1); // установила эпику Id 1
        epic.addSubtask(1); // пытаюсь добавить ID эпика в список подзадач
        assertEquals(0, epic.getSubtaskId().size()); // не должно добавиться, список подзадач должен быть 0
    }

}