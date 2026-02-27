package com.library.util;

import com.library.service.LibraryService;

/**
 * Thread for simulating book issue operations
 * Demonstrates concurrent operations
 */
public class BookIssueThread extends Thread {
    private LibraryService libraryService;
    private String bookId;
    private String userName;

    public BookIssueThread(LibraryService libraryService, String bookId, String userName) {
        super("BookIssueThread-" + userName);
        this.libraryService = libraryService;
        this.bookId = bookId;
        this.userName = userName;
    }

    @Override
    public void run() {
        try {
            System.out.println("[" + userName + "] Attempting to issue book: " + bookId);
            libraryService.issueBook(bookId);
            System.out.println("[" + userName + "] Successfully issued book: " + bookId);
            Thread.sleep(100); // Simulate processing time
        } catch (Exception e) {
            System.out.println("[" + userName + "] Failed to issue book: " + e.getMessage());
        }
    }
}

