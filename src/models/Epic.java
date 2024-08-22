package models;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Long> subTaskIds = new ArrayList<>();

    public Epic(Long id, String name, String description) {
        super(id, name, description);
    }

    public Epic(String name, String description) {
        super(name, description);
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
