package com.library.model;

/**
 * Represents an eBook in the library system
 * Demonstrates inheritance and method overriding
 */
public class EBook extends Book {
    private String fileFormat; // PDF, EPUB, etc.
    private double fileSizeMB;

    // Constructor
    public EBook(String bookId, String title, String author, String category, 
                 int totalCopies, String fileFormat, double fileSizeMB) {
        super(bookId, title, author, category, totalCopies);
        this.fileFormat = fileFormat;
        this.fileSizeMB = fileSizeMB;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public double getFileSizeMB() {
        return fileSizeMB;
    }

    public void setFileSizeMB(double fileSizeMB) {
        this.fileSizeMB = fileSizeMB;
    }

    // Override issueBook() method for ebooks
    @Override
    public void issueBook() {
        // EBooks can be issued to multiple users simultaneously
        // No limit on copies for ebooks
        System.out.println("EBook '" + getTitle() + "' downloaded successfully!");
        System.out.println("Format: " + fileFormat + ", Size: " + fileSizeMB + " MB");
    }

    // Override borrowDuration() from Borrowable interface
    @Override
    public int borrowDuration() {
        return 21; // 21 days for ebooks (longer than physical books)
    }

    @Override
    public String getFormattedDetails() {
        return super.getFormattedDetails() + 
               String.format("\nFile Format: %s\nFile Size: %.2f MB\nType: EBook", 
                           fileFormat, fileSizeMB);
    }

    @Override
    public String toString() {
        return String.format("EBook[ID=%s, Title=%s, Author=%s, Format=%s, Size=%.2fMB]",
                getBookId(), getTitle(), getAuthor(), fileFormat, fileSizeMB);
    }
}

