package models;

import enums.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Long> subTaskIds = new ArrayList<>();
    private LocalDateTime endTime = null;

    public Epic(Long id, String name, String description) {
        super(id, name, description);
    }

    public Epic(Long id, String name, String description,
                TaskStatus status, LocalDateTime startTime, Duration duration) {
        super(id, name, description, status, startTime, duration);
    }

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(Long id, String name, String description, TaskStatus status, LocalDateTime startTime,
                LocalDateTime endTime, Duration duration) {
        super(id, name, description, status, startTime, duration);
        this.endTime = endTime;
    }

    public void setDuration(Long duration) {
        this.duration = Duration.ofMinutes(duration);
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public List<Long> getSubTaskIds() {
        return subTaskIds;
    }

    public void setSubTaskId(long id) {
        if (!subTaskIds.contains(id)) {
            subTaskIds.add(id);
        }
    }

    public void setSubTaskIds(List<Long> ids) {
        if (subTaskIds.isEmpty()) {
            subTaskIds.addAll(ids);
        }
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + this.getId() +
                ", name='" + this.getName() + '\'' +
                ", description='" + this.getDescription() + '\'' +
                ", status=" + this.getStatus() +
                ", subtaskIds=" + subTaskIds +
                '}';
    }
}
