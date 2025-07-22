package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {

    private String title;
    private String description;
    protected int id;
    protected TaskStatus status;
    protected LocalDateTime startTime;
    protected Duration duration;

    public Task(String title, String description) {
        this.id = 0;
        this.title = title;
        this.description = description;
        this.status = TaskStatus.NEW;

    }

    public Task(int id, String title, String description, TaskStatus status, LocalDateTime startTime, Duration duration) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = TaskStatus.NEW;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public static Task copyTask(Task original) {
        if (original == null) return null;
        Task copyOfTask = new Task(original.getId(), original.getTitle(), original.getDescription(),
                original.getStatus(), original.getStartTime(), original.getDuration());

        return copyOfTask;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }


    @Override
    public String toString() {
        return "\nTask{" +
                "id= " + id +
                "| title= '" + title + '\'' +
                "| description= '" + description + '\'' +
                "| status= " + status +
                "| startTime= " + (startTime != null ? startTime : "null") +
                "| duration= " + (duration != null ? duration.toMinutes() + "min" : "null") +
                "| endTime= " + (getEndTime() != null ? getEndTime() : "null") +
                '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(id);
    }
}
