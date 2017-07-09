package in.codingninjas.todoapp;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by nsbhasin on 09/07/17.
 */

class Todo implements Serializable {
    private long id;
    private String title;
    private String category;
    private Date date;
    private int priority;
    private String label;
    private boolean hasReminder;

    Todo(long id, String title, String category, Date date, int priority, String label, boolean hasReminder) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.date = date;
        this.priority = priority;
        this.label = label;
        this.hasReminder = hasReminder;
    }

    Todo(String title, String category, Date date, int priority, String label, boolean hasReminder) {
        this.title = title;
        this.category = category;
        this.date = date;
        this.priority = priority;
        this.label = label;
        this.hasReminder = hasReminder;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getCategory() {
        return category;
    }

    void setCategory(String category) {
        this.category = category;
    }

    Date getDate() {
        return date;
    }

    void setDate(Date date) {
        this.date = date;
    }

    int getPriority() {
        return priority;
    }

    void setPriority(int priority) {
        this.priority = priority;
    }

    String getLabel() {
        return label;
    }

    void setLabel(String label) {
        this.label = label;
    }

    boolean hasReminder() {
        return hasReminder;
    }

    void setReminder(boolean hasReminder) {
        this.hasReminder = hasReminder;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
