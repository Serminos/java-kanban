package models;

import enums.TaskStatus;

public class SubTask extends Task {

    private final long epicId;

    public SubTask(long epicId, String name, String description, TaskStatus status) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public SubTask(long subTaskId, long epicId, String name, String description, TaskStatus status) {
        super(subTaskId, name, description, status);
        this.epicId = epicId;
    }

    public long getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "epicId=" + this.getEpicId() +
                ", id=" + this.getId() +
                ", name='" + this.getName() + '\'' +
                ", description='" + this.getDescription() + '\'' +
                ", status=" + this.getStatus() +
                '}';
    }
}
