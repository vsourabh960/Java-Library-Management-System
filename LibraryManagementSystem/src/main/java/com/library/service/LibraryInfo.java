package com.library.service;

public class LibraryInfo {
    private final String name;
    private final String location;
    private final int bookCount;

    public LibraryInfo(String name, String location, int bookCount) {
        this.name = name;
        this.location = location;
        this.bookCount = bookCount;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public int getBookCount() {
        return bookCount;
    }
}
