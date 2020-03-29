package com.kraigs.android.micamp.Home.Updates;

public class Update {
    private String updateName;
    private String updateUrl;

    private Update(){

    }

    public Update(String updateName,String updateUrl){
        this.updateName = updateName;
        this.updateUrl = updateUrl;
    }

    public String getUpdateName() {
        return updateName;
    }

    public void setUpdateName(String updateName) {
        this.updateName = updateName;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }
}
