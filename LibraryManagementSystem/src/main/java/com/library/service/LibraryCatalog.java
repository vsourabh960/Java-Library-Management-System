package com.library.service;

import com.library.model.Book;

import java.util.ArrayList;
import java.util.List;

class LibraryCatalog {
    final String name;
    String location;
    final List<Book> books;

    LibraryCatalog(String name, String location) {
        this.name = name;
        this.location = location;
        this.books = new ArrayList<>();
    }
}
