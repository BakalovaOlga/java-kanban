package tasks;

import java.util.Objects;

public class Task {

    protected String title;
    protected String description;
    protected int id;
    protected TaskStatus status;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = TaskStatus.NEW;
    }
    public Task(int id, String title, String description, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }
    public Task(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
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

//    @Override
//    public final boolean equals(Object obj) {
//        if (this == obj) return true; // проверяем адреса объектов
//        if (obj == null) return false; // проверяем ссылку на null
//        if (this.getClass() != obj.getClass()) return false; // сравниваем классы
//        Task otherTask = (Task) obj; // открываем доступ к полям другого объекта
//        return  (id == otherTask.id) &&
//                Objects.equals(title, otherTask.title) &&
//                Objects.equals(description, otherTask.description) &&
//                Objects.equals(status, otherTask.status);
//    }

//    @Override
//    public final int hashCode() {
//        int hash = 11;
//        if (title != null) {
//            hash = hash + title.hashCode();
//        }
//        hash = hash * 31;
//        if (description != null) {
//            hash = hash + description.hashCode();
//        }
//        hash = hash * 31;
//        if (status != null) {
//            hash = hash + status.hashCode();
//        }
//
//        return hash; // возвращаем итоговый хеш
//    }

    @Override
    public String toString() {
        return "\nTask{" +
                "id= " + id +
                "| title= '" + title + '\'' +
                "| description= '" + description + '\'' +
                "| status= " + status +
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
