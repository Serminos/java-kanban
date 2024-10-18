import enums.TaskStatus;
import manager.*;
import models.Epic;
import models.SubTask;
import models.Task;

import java.util.*;

public class Main {
    static TaskManager taskManager;

    public static void main(String[] args) {
        System.out.println("Поехали!");
        taskManager = Managers.getDefault();
        for (int i = 0; i < 100; i++) {
            optionalHistoryCase();
        }
    }

    public static void optionalHistoryCase() {
        int countViewEpic1 = 0;
        int countViewSubTaskId1 = 0;
        int countViewSubTaskId2 = 0;
        int countViewSubTaskId3 = 0;
        int countViewEpic2 = 0;
        // Создайте две задачи, эпик с тремя подзадачами и эпик без подзадач.
        final Long epicId1 = taskManager.create(new Epic("Испечь торт", "Испечь торт Наполеон"));
        final Long subTaskId1 = taskManager.create(new SubTask(epicId1, "Найти рецепт ютуб",
                "Выполнить поиск видео рецепта", TaskStatus.NEW));
        final Long subTaskId2 = taskManager.create(new SubTask(epicId1, "Найти рецепт библиотека",
                "Выполнить поиск видео рецепта в книгах", TaskStatus.NEW));
        final Long subTaskId3 = taskManager.create(new SubTask(epicId1, "Найти рецепт знакомые",
                "Опросить знакомых", TaskStatus.NEW));
        final Long epicId2 = taskManager.create(new Epic("Испечь торт", "Испечь торт Красный бархат"));
        // Запросите созданные задачи несколько раз в разном порядке.
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            switch (random.nextInt(5)) {
                case 0:
                    taskManager.getEpic(epicId1);
                    countViewEpic1 = (countViewEpic1 == 0) ? 1 : countViewEpic1;
                    break;
                case 1:
                    taskManager.getSubTask(subTaskId1);
                    countViewSubTaskId1 = (countViewSubTaskId1 == 0) ? 1 : countViewSubTaskId1;
                    break;
                case 2:
                    taskManager.getSubTask(subTaskId2);
                    countViewSubTaskId2 = (countViewSubTaskId2 == 0) ? 1 : countViewSubTaskId2;
                    break;
                case 3:
                    taskManager.getSubTask(subTaskId3);
                    countViewSubTaskId3 = (countViewSubTaskId3 == 0) ? 1 : countViewSubTaskId3;
                    break;
                case 4:
                    taskManager.getEpic(epicId2);
                    countViewEpic2 = (countViewEpic2 == 0) ? 1 : countViewEpic2;
                    break;
            }
            // После каждого запроса выведите историю и убедитесь, что в ней нет повторов.
            int countView = countViewEpic1 + countViewSubTaskId1 + countViewSubTaskId2 + countViewSubTaskId3 + countViewEpic2;
            Set<Long> set = new HashSet<>();
            List<Long> duplicates = new ArrayList<>();
            taskManager.getHistory().forEach(task -> {
                if (!set.add(task.getId())) {
                    duplicates.add(task.getId());
                }
            });
            if (!duplicates.isEmpty()) {
                System.out.println("В истории просмотра задач имеются дубли. Ожидаемое значение: " + countView +
                        ", полученное значение: " + taskManager.getHistory().size());
            } else {
                System.out.println("Номер итерации: " + i +
                        " Задач в истории: " + taskManager.getHistory().size());
            }
        }
        // Удалите задачу, которая есть в истории, и проверьте, что при печати она не будет выводиться.
        taskManager.getEpic(epicId2);
        taskManager.removeEpic(epicId2);
        for (Task task : taskManager.getHistory()) {
            if (Objects.equals(task.getId(), epicId2)) {
                System.out.println("После удаления epicId2 история по нему НЕ очистилась");
            }
        }
        // Удалите эпик с тремя подзадачами и убедитесь, что из истории удалился как сам эпик, так и все его подзадачи.
        taskManager.getEpic(epicId1);
        taskManager.removeEpic(epicId1);
        for (Task task : taskManager.getHistory()) {
            if (Objects.equals(task.getId(), epicId1)
                    || Objects.equals(task.getId(), subTaskId1)
                    || Objects.equals(task.getId(), subTaskId2)
                    || Objects.equals(task.getId(), subTaskId3)
            ) {
                System.out.println("После удаления epicId1 история по нему НЕ очистилась");
            }
        }
        if (taskManager.getHistory().isEmpty()) {
            System.out.println("Вся история очищена верно");
        }
    }
}
