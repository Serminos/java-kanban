package models;

import enums.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {

    @Test
    void testEquals_DifferentEpicShouldBeEqualsById() {
        // prepare
        Epic epic1 = new Epic(1L, "Приготовить завтрак", "Сварить макароны и пожарить котлету");
        SubTask subTask1 = new SubTask(2L, 1L, "Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        SubTask subTask2 = new SubTask(2L, 1L, "Приготовить ужин",
                "Пить воду", TaskStatus.NEW);
        SubTask subTask3 = new SubTask(3L, 1L, "Приготовить ужин",
                "Пить воду", TaskStatus.NEW);

        //check
        assertEquals(subTask1, subTask2);
        assertNotEquals(subTask2, subTask3);
    }

}