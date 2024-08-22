package manager;

import models.Epic;
import models.SubTask;
import models.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    protected final List<Task> history = new ArrayList<>();
    final int lengthHistory = 10;

    /**
     * @param task
     */
    @Override
    public void add(Task task) {
        if (task == null) return;
        if (history.size() == lengthHistory) {
            history.remove(0);
        }
        history.add(task);
    }

    /**
     * @return List<Task>
     */
    @Override
    public List<Task> getHistory() {
        List<Task> listCopy = new ArrayList<>();
        for (Task task : history) {
            if (task instanceof SubTask actualSubTask) {
                SubTask subTaskCopy = new SubTask(actualSubTask.getEpicId(), actualSubTask.getName(),
                        actualSubTask.getDescription(), actualSubTask.getStatus());
                subTaskCopy.setId(actualSubTask.getId());
                listCopy.add(subTaskCopy);
            } else if (task instanceof Epic actualEpicTask) {
                Epic epicCopy = new Epic(actualEpicTask.getName(), actualEpicTask.getDescription());
                epicCopy.setId(actualEpicTask.getId());
                epicCopy.setStatus(actualEpicTask.getStatus());
                epicCopy.setSubTaskIds(actualEpicTask.getSubTaskIds());
                listCopy.add(epicCopy);
            } else if (task instanceof Task) {
                Task taskCopy = new Task(task.getName(), task.getDescription(), task.getStatus());
                taskCopy.setId(task.getId());
                listCopy.add(taskCopy);
            }
        }
        return listCopy;
    }
}
