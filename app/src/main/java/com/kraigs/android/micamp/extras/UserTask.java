package com.kraigs.android.micamp.extras;

public class UserTask {
    private String taskName;
    private String taskUrl;

    public UserTask(){

    }


    public UserTask(String taskName,String taskUrl) {
        this.taskName = taskName;
        this.taskUrl = taskUrl;

    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskUrl() {
        return taskUrl;
    }

    public void setTaskUrl(String taskUrl) {
        this.taskUrl = taskUrl;
    }
}
