import enums.TaskStatus;
import manager.TaskManager;
import models.Epic;
import models.SubTask;
import models.Task;

import java.util.List;

public class Main {
    static TaskManager taskManager = new TaskManager();

    public static void main(String[] args) {
        System.out.println("Поехали!");
        //testTaskManager();
    }

    private static void testTaskManager() {
        System.out.println("---> testAddTasksAllType");
        testAddTasksAllType();
        printAllTasks();
        System.out.println("---> clearTaskManager");
        clearTaskManager();
        printAllTasks();
        System.out.println("");
        System.out.println("---> testGet");
        testAddTasksAllType();
        System.out.println("---> testGetById ");
        testGetById();
        System.out.println("---> testGetSubTaskByEpicID ");
        testGetSubTaskByEpicID();
        clearTaskManager();
        System.out.println("");
        System.out.println("---> case1");
        testCase1();
        taskManager.clear();
    }

    private static void testAddTasksAllType() {
        taskManager.update(new Task("Task1", "Task1 description", TaskStatus.NEW));
        taskManager.update(new Task("Task2", "Task2 description", TaskStatus.IN_PROGRESS));
        taskManager.update(new Task("Task3", "Task3 description", TaskStatus.DONE));

        Long epic1Id = taskManager.update(new Epic("Epic1", "Epic1 description"));
        Long epic2Id = taskManager.update(new Epic("Epic2", "Epic2 description"));
        Long epic3Id = taskManager.update(new Epic("Epic3", "Epic3 description"));

        taskManager.update(new SubTask(epic1Id,
                "SubTask1-1", "Epic1 - SubTask1 description", TaskStatus.NEW));
        taskManager.update(new SubTask(epic1Id,
                "SubTask1-2", "Epic1 - SubTask2 description", TaskStatus.IN_PROGRESS));
        taskManager.update(new SubTask(epic2Id,
                "SubTask2-1", "Epic2 - SubTask1 description", TaskStatus.DONE));
        taskManager.update(new SubTask(epic3Id,
                "SubTask3-1", "Epic3 - SubTask1 description", TaskStatus.DONE));
    }

    private static void testCase1() {
        // Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        System.out.println("---> case1 Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.");
        Long task1Id = taskManager.update(new Task("Task1", "Task1 description", TaskStatus.NEW));
        Long task2Id = taskManager.update(new Task("Task2", "Task2 description", TaskStatus.IN_PROGRESS));

        Long epic1Id = taskManager.update(new Epic("Epic1", "Epic1 description"));
        Long epic2Id = taskManager.update(new Epic("Epic2", "Epic2 description"));

        Long subTask11Id = taskManager.update(new SubTask(epic1Id,
                "SubTask1-1", "Epic1 - SubTask1 description", TaskStatus.NEW));
        Long subTask12Id = taskManager.update(new SubTask(epic1Id,
                "SubTask1-2", "Epic1 - SubTask2 description", TaskStatus.DONE));
        Long subTask21Id = taskManager.update(new SubTask(epic2Id,
                "SubTask2-1", "Epic2 - SubTask1 description", TaskStatus.DONE));
        // Распечатайте списки эпиков, задач и подзадач через
        System.out.println("---> case1 Распечатайте списки эпиков, задач и подзадач через");
        printAllTasks();
        System.out.println("---> case1 Измените статусы созданных объектов, распечатайте их.");
        // Измените статусы созданных объектов, распечатайте их.
        taskManager.update(new Task(task1Id, "Task1", "Task1 description", TaskStatus.IN_PROGRESS));
        taskManager.update(new Task(task2Id, "Task2", "Task2 description", TaskStatus.DONE));

        taskManager.update(new SubTask(subTask11Id, epic1Id,
                "SubTask1-1", "Epic1 - SubTask1 description", TaskStatus.IN_PROGRESS));
        taskManager.update(new SubTask(subTask12Id, epic1Id,
                "SubTask1-2", "Epic1 - SubTask2 description", TaskStatus.DONE));
        taskManager.update(new SubTask(subTask21Id, epic2Id,
                "SubTask2-1", "Epic2 - SubTask1 description", TaskStatus.DONE));
        printAllTasks();
        // Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
        taskManager.removeTask(task2Id);
        taskManager.removeEpic(epic2Id);
        taskManager.removeSubTask(subTask12Id);
        System.out.println("---> case1 И, наконец, попробуйте удалить одну из задач и один из эпиков.");
        printAllTasks();
        // И, наконец, попробуйте удалить одну из задач и один из эпиков.
    }

    private static void printAllTasks() {
        List<String> tasksText = taskManager.getAllTasksText();
        if (tasksText.isEmpty()) {
            System.out.println("No task");
        }
        for (String s : tasksText) {
            System.out.println(s);
        }
    }

    private static void clearTaskManager() {
        taskManager.clear();
    }

    private static void testGetById() {
        for (long i = 1; i < 10; i++) {
            System.out.println(taskManager.getById(i));
        }
    }

    private static void testGetSubTaskByEpicID() {
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(taskManager.getAllSubTasksByEpicId(epic.getId()));
        }
    }
}
