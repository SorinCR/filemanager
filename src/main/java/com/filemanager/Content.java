package com.filemanager;

public abstract class Content {
    private String name;

    public Content(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }
}
