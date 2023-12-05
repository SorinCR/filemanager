package com.filemanager;

import java.util.ArrayList;
import java.util.List;

public class Directory extends Content {
    private List<Directory> subDirectories;
    private List<Content> subFiles;

    public Directory(String name) {
        super(name);
        this.subDirectories = new ArrayList<Directory>();
        this.subFiles = new ArrayList<Content>();
    }

    public List<Directory> getSubDirectories() {
        return subDirectories;
    }

    public void addSubDirectory(Directory dir) {
        subDirectories.add(dir);
    }

    public List<Content> getSubFiles() {
        return subFiles;
    }

    public void addSubFile(Content file) {
        subFiles.add(file);
    }

}
