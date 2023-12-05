package com.filemanager;

public class RegularFile extends Content {
    private String type;
    private double size;

    public RegularFile(String name, String type, double size) {
        super(name);
        this.type = type;
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSizeInBytes() {
        return String.format("%.2fB", size);
    }

    public String getSizeInKilobytes() {
        return String.format("%.2fKB", size / 1024);
    }

    public String getSizeInMegabytes() {
        return String.format("%.2fMB", size / (1024 * 1024));
    }

    public String getSizeInGigabytes() {
        return String.format("%.2fGB", size / (1024 * 1024 * 1024));
    }

    public String getReadableSize() {
        if (size / (1024 * 1024 * 1024) > 1) {
            return getSizeInGigabytes();
        } else if (size / (1024 * 1024) > 1) {
            return getSizeInMegabytes();
        } else if (size / 1024 > 1) {
            return getSizeInKilobytes();
        } else {
            return getSizeInBytes();
        }
    }

}
