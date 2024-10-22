package manager;

import enums.TaskStatus;
import exception.ManagerSaveException;
import models.Epic;
import models.SubTask;
import models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File file;
    private static final String CSV_HEADER = "id,type,name,status,description,epic";

    @BeforeEach
    void beforeEachTest() {
        try {
            file = File.createTempFile("test", ".csv");
            file.deleteOnExit();
            taskManager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать файл", e);
        }
    }

    @Test
    void shouldExceptionWhenLoadEmptyFile() {
        // do
        assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(file);
        }, "Проверка указанного файла должна выдать исключение: " + file.getAbsolutePath());
    }

    @Test
    void shouldExceptionWhenLoadIfFileNotExist() {
        // do
        assertThrows(ManagerSaveException.class, () -> {
            file.delete();
            FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(file);
        }, "Проверка указанного файла должна выдать исключение: " + file.getAbsolutePath());
    }

    @Test
    void shouldExceptionWhenLoadIfWrongHeaderFormat() {
        // do
        assertThrows(ManagerSaveException.class, () -> {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(CSV_HEADER + ",wrongcolumn" + "\n");
            } catch (Exception ignored) {
            }
            FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(file);
        }, "Формат CSV-файла не соответствует. Должно выдать исключение: " + file.getAbsolutePath());
    }

    @Test
    void shouldExceptionWhenLoadIfWrongDataFormat() {
        // do
        assertThrows(ManagerSaveException.class, () -> {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(CSV_HEADER + "\n");
                writer.write("1,TASK123,Приготовить завтрак,NEW,Сварить макароны и пожарить котлету," + "\n");
            } catch (Exception ignored) {
            }
            FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(file);
        }, "Проверка указанного файла должна выдать исключение: " + file.getAbsolutePath());
    }

    @Test
    void shouldExceptionWhenSaveWrongPath() {
        assertThrows(ManagerSaveException.class, () -> {
            // prepare
            file = File.createTempFile("test", ".csv");
            file.delete();
            taskManager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);
            Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету",
                    TaskStatus.NEW, LocalDateTime.of(2024, 10, 21, 19, 0),
                    Duration.ofMinutes(60)
            );

            // do
            final Long savedTaskId = taskManager.create(task);
        }, "Проверка указанного файла должна выдать исключение: " + file.getAbsolutePath());
    }

    @Test
    void shouldNotExceptionIfDoesNotHaveData() {
        // do
        try {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(CSV_HEADER + "\n");
            } catch (Exception ignored) {
            }
            FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(file);
        } catch (ManagerSaveException e) {
            //check
            Assertions.fail("Проверка не должна выдать исключение");
        }
    }

    @Test
    void shouldSaveAndLoadEmptyTasks() {
        // prepare
        Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету",
                TaskStatus.NEW, LocalDateTime.of(2024, 10, 21, 19, 0),
                Duration.ofMinutes(60)
        );
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedTaskId = taskManager.create(task);
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW,
                LocalDateTime.of(2024, 11, 21, 19, 0), Duration.ofMinutes(60)
        );
        final Long savedSubTaskId = taskManager.create(subTask);
        taskManager.clear();

        // do
        FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(file);

        //check
        assertEquals(0, testManager.getTasks().size());
        assertEquals(0, testManager.getEpics().size());
        assertEquals(0, testManager.getSubTasks().size());
    }


    @Test
    void shouldSaveAndLoadDifferentTasks() {
        // prepare
        Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету",
                TaskStatus.NEW, LocalDateTime.of(2024, 10, 21, 19, 0),
                Duration.ofMinutes(60)
        );
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedTaskId = taskManager.create(task);
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW,
                LocalDateTime.of(2024, 11, 21, 19, 0), Duration.ofMinutes(60)
        );
        final Long savedSubTaskId = taskManager.create(subTask);

        // do
        FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(file);

        //check
        assertEquals(taskManager.getTasks().size(), testManager.getTasks().size());
        assertEquals(taskManager.getTasks().get(0), testManager.getTasks().get(0));
        assertEquals(taskManager.getEpics().size(), testManager.getEpics().size());
        assertEquals(taskManager.getEpics().get(0), testManager.getEpics().get(0));
        assertEquals(taskManager.getSubTasks().size(), testManager.getSubTasks().size());
        assertEquals(taskManager.getSubTasks().get(0), testManager.getSubTasks().get(0));
    }
}