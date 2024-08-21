package models;

import enums.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void testEquals_DifferentTaskShouldBeEqualsById() {
        // prepare
        Task task1 = new Task(1L, "Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        Task task2 = new Task(1L, "Приготовить ужин", "Пить воду", TaskStatus.NEW);
        Task task3 = new Task(2L, "Приготовить ужин", "Пить воду", TaskStatus.NEW);

        //check
        assertEquals(task1, task2);
        assertNotEquals(task2, task3);
    }
}