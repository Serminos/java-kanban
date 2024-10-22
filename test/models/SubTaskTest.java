package models;

import enums.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {

    @Test
    void testEquals_DifferentEpicShouldBeEqualsById() {
        // prepare
        Epic epic1 = new Epic(1L, "Приготовить завтрак", "Сварить макароны и пожарить котлету");
        SubTask subTask1 = new SubTask(2L, 1L, "Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60)
        );
        SubTask subTask2 = new SubTask(2L, 1L, "Приготовить ужин",
                "Пить воду", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60)
        );
        SubTask subTask3 = new SubTask(3L, 1L, "Приготовить ужин",
                "Пить воду", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60)
        );

        //check
        assertEquals(subTask1, subTask2);
        assertNotEquals(subTask2, subTask3);
    }

}