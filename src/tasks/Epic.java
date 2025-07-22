package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskId; //список Id подзадач
    private LocalDateTime endTime;

    public Epic(int id, String title, String description, TaskStatus status,
                LocalDateTime startTime, Duration duration) {
        super(id, title, description, status, startTime, duration);
        this.subtaskId = new ArrayList<>();
    }

    public Epic(int id, String title, String description) {
        super(id, title, description);
        this.subtaskId = new ArrayList<>();
    }

    public Epic(String title, String description) {
        super(title, description);
        this.subtaskId = new ArrayList<>();
    }

    public List<Integer> getSubtaskId() {
        return subtaskId;
    }

    public void setSubtaskId(ArrayList<Integer> subtaskId) {
        this.subtaskId = subtaskId;
    }

    public void addSubtask(int subId) {
        if (subId == this.getId() || subtaskId.contains(subId)) {
            return;
        }
        subtaskId.add(subId);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        return "\nEpic{" +
                ", " + super.toString() +
                ", subtaskId=" + subtaskId +
                '}';
    }
}
