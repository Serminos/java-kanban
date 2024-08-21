package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void testEquals_DifferentEpicShouldBeEqualsById() {
        // prepare
        Epic epic1 = new Epic(1L, "Приготовить завтрак", "Сварить макароны и пожарить котлету");
        Epic epic2 = new Epic(1L, "Приготовить ужин", "Пить воду");
        Epic epic3 = new Epic(2L, "Приготовить ужин", "Пить воду");

        //check
        assertEquals(epic1, epic2);
        assertNotEquals(epic2, epic3);
    }
}