package com.library.model;

import com.library.interfaces.Borrowable;

/**
 * Represents a book in the library system
 * Demonstrates encapsulation, constructors, and string handling
 */
public class Book implements Borrowable {
    // Private fields for encapsulation
    private String bookId;
    private String title;
    private String author;
    private String category;
    private int totalCopies;
    private int issuedCopies;
    private int remainingCopies;
    private boolean isAvailable;
    
    // Static variable to track total number of books
    private static int totalBooksCount = 0;

    // Constructor to initialize book details
    public Book(String bookId, String title, String author, String category, int totalCopies) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.totalCopies = totalCopies;
        this.issuedCopies = 0;
        this.remainingCopies = totalCopies; // Arithmetic operation: remaining = total - issued
        this.isAvailable = totalCopies > 0;
        totalBooksCount++; // Increment static counter
    }

    // Public getters and setters
    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
        // Recalculate remaining copies
        this.remainingCopies = totalCopies - issuedCopies;
    }

    public int getIssuedCopies() {
        return issuedCopies;
    }

    public void setIssuedCopies(int issuedCopies) {
        this.issuedCopies = issuedCopies;
        this.remainingCopies = totalCopies - issuedCopies; // Arithmetic operation
        this.isAvailable = remainingCopies > 0;
    }

    public int getRemainingCopies() {
        return remainingCopies;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    // Static method to get total books count
    public static int getTotalBooksCount() {
        return totalBooksCount;
    }

    // Method to issue a book
    public void issueBook() {
        if (isAvailable && remainingCopies > 0) {
            issuedCopies++; // Arithmetic operation: increment issued
            remainingCopies = totalCopies - issuedCopies; // Arithmetic operation: calculate remaining
            isAvailable = remainingCopies > 0; // Condition check
            System.out.println("Book issued successfully!");
        } else {
            System.out.println("Book is not available for issue.");
        }
    }

    // Method to return a book
    public void returnBook() {
        if (issuedCopies > 0) {
            issuedCopies--; // Arithmetic operation: decrement issued
            remainingCopies = totalCopies - issuedCopies; // Arithmetic operation: calculate remaining
            isAvailable = remainingCopies > 0; // Condition check
            System.out.println("Book returned successfully!");
        } else {
            System.out.println("No copies are currently issued.");
        }
    }

    // String handling: Convert title to uppercase
    public String getTitleUpperCase() {
        return title.toUpperCase();
    }

    // String handling: Compare book titles without case sensitivity
    public boolean compareTitleIgnoreCase(String otherTitle) {
        return this.title.equalsIgnoreCase(otherTitle);
    }

    // String handling: Extract author's last name
    public String getAuthorLastName() {
        if (author == null || author.trim().isEmpty()) {
            return "";
        }
        String[] nameParts = author.trim().split("\\s+");
        return nameParts.length > 0 ? nameParts[nameParts.length - 1] : "";
    }

    // Formatted book details display
    public String getFormattedDetails() {
        return String.format(
            "═══════════════════════════════════════\n" +
            "Book ID: %s\n" +
            "Title: %s\n" +
            "Author: %s (Last Name: %s)\n" +
            "Category: %s\n" +
            "Total Copies: %d\n" +
            "Issued Copies: %d\n" +
            "Remaining Copies: %d\n" +
            "Available: %s\n" +
            "═══════════════════════════════════════",
            bookId, getTitleUpperCase(), author, getAuthorLastName(),
            category, totalCopies, issuedCopies, remainingCopies,
            isAvailable ? "Yes" : "No"
        );
    }

    // Implementation of Borrowable interface
    @Override
    public int borrowDuration() {
        return 14; // 14 days for physical books
    }

    @Override
    public String toString() {
        return String.format("Book[ID=%s, Title=%s, Author=%s, Available=%s, Remaining=%d]",
                bookId, title, author, isAvailable, remainingCopies);
    }
}

