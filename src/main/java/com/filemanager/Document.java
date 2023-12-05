package com.filemanager;

public class Document extends RegularFile implements Readable {
    private String content;

    public Document(String name, String type, double size, String content) {
        super(name, type, size);
        this.content = content;
    }

    public void read() {
        System.out.println(content.length() == 0 ? "Empty Document" : content);
    };
}
