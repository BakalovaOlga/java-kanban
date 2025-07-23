package tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId; // эпик, на который ссылается подзадача

    public Subtask(int id, String title, String description, TaskStatus status, int epicId,
                   LocalDateTime startTime, Duration duration) {
        super(id, title, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask(String title, String description) {
        super(title, description);
        this.status = TaskStatus.NEW;

    }

    public Subtask(int id, String title, String description, int epicId) {
        super(id, title, description);
        this.epicId = epicId;
        this.status = TaskStatus.NEW;
    }

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
        this.status = TaskStatus.NEW;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "\nSubtask{" +
                " id=" + id +
                ", epicId=" + epicId +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + status +
                "| startTime= " + (startTime != null ? startTime : "null") +
                "| duration= " + (duration != null ? duration.toMinutes() + "min" : "null") +
                "| endTime= " + (getEndTime() != null ? getEndTime() : "null") +
                '}';
    }
}
