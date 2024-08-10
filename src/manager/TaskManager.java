package manager;

import enums.TaskStatus;
import models.Epic;
import models.SubTask;
import models.Task;

import java.util.*;

public class TaskManager {
    protected long id;
    protected final Map<Long, Task> tasks = new HashMap<>();
    protected final Map<Long, Epic> epics = new HashMap<>();
    protected final Map<Long, SubTask> subTasks = new HashMap<>();

    public long nextId() {
        return ++id;
    }

    public Long update(Task task) {
        if (task.getId() == null) {
            long newTaskId = nextId();
            task.setId(newTaskId);
        }
        tasks.put(task.getId(), task);
        return task.getId();
    }

    public Long update(Epic epic) {
        if (epic.getId() == null) {
            long newTaskId = nextId();
            epic.setId(newTaskId);
        }
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    public Long update(SubTask subTask) {
        long newSubTaskId;
        if (subTask.getId() == null) {
            newSubTaskId = nextId();
            subTask.setId(newSubTaskId);
        }
        newSubTaskId = subTask.getId();
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            subTasks.put(newSubTaskId, subTask);
            epic.setSubTaskIds(newSubTaskId);
            updateEpicStatus(epic);
            return newSubTaskId;
        }
        return null;
    }

    public Collection<Task> getTasks() {
        return tasks.values();
    }

    public Collection<Epic> getEpics() {
        return epics.values();
    }

    public Collection<SubTask> getSubTasks() {
        return subTasks.values();
    }

    public Task getTask(Long id) {
        return tasks.getOrDefault(id, null);
    }

    public Epic getEpic(Long id) {
        return epics.getOrDefault(id, null);
    }

    public List<SubTask> getAllSubTasksByEpicId(Long id) {
        Epic epic = epics.get(id);
        List<SubTask> epicSubTasks = new ArrayList<>();
        if (epic != null) {
            epic.getSubTaskIds().forEach(subTaskId -> epicSubTasks.add(subTasks.get(subTaskId)));
            return epicSubTasks;
        }
        return epicSubTasks;
    }

    public SubTask getSubTask(Long id) {
        return subTasks.getOrDefault(id, null);
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearEpics() {
        epics.clear();
    }

    public void clearSubTasks() {
        subTasks.clear();
    }

    public void clear() {
        clearTasks();
        clearEpics();
        clearSubTasks();
        id = 0;
    }

    public void removeTask(Long id) {
        tasks.remove(id);
    }

    public void removeEpic(Long id) {
        Epic epic = this.getEpic(id);
        if (epic != null) {
            for (Long subtaskId : epic.getSubTaskIds()) {
                subTasks.remove(subtaskId);
            }
            epics.remove(id);
        }
    }

    public void removeSubTask(Long id) {
        SubTask subTask = subTasks.get(id);
        if (subTask != null) {
            Epic epic = getEpic(subTask.getEpicId());
            subTasks.remove(id);
            epic.getSubTaskIds().remove(subTask.getId());
            updateEpicStatus(epic);
        }
    }

    public void updateEpicStatus(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            return;
        }

        long epicSubTaskSize = epic.getSubTaskIds().size();
        if (epicSubTaskSize == 0) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        List<SubTask> epicSubTasks = new ArrayList<>();
        int cntDone = 0;
        int cntNew = 0;

        epic.getSubTaskIds().forEach(subTaskId -> epicSubTasks.add(subTasks.get(subTaskId)));

        for (SubTask subTask : epicSubTasks) {
            if (subTask.getStatus() == TaskStatus.IN_PROGRESS) {
                epic.setStatus(TaskStatus.IN_PROGRESS);
                return;
            }
            if (subTask.getStatus() == TaskStatus.DONE) {
                cntDone++;
            }
            if (subTask.getStatus() == TaskStatus.NEW) {
                cntNew++;
            }
            if (cntDone != 0 && cntNew != 0) {
                epic.setStatus(TaskStatus.IN_PROGRESS);
                return;
            }
        }

        if (cntDone == epicSubTaskSize) {
            epic.setStatus(TaskStatus.DONE);
        } else if (cntNew == epicSubTaskSize) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    public List<String> getAllTasksText() {
        List<String> resultText = new ArrayList<>();
        this.getTasks().forEach(v -> resultText.add(v.toString()));
        this.getEpics().forEach(v -> resultText.add(v.toString()));
        this.getSubTasks().forEach(v -> resultText.add(v.toString()));
        return resultText;
    }

    public Task getById(Long id) {
        if (this.getTask(id) != null) return this.getTask(id);
        if (this.getEpic(id) != null) return this.getEpic(id);
        if (this.getSubTask(id) != null) return this.getSubTask(id);
        return null;
    }
}
