package main.java.com.library;

import com.library.model.Book;
import com.library.model.EBook;
import com.library.service.LibraryInfo;
import com.library.service.LibraryService;
import com.library.util.BookIssueThread;
import com.library.util.NotificationThread;

import java.util.List;
import java.util.Scanner;

/**
 * Main application class for Library Management System.
 */
public class LibraryApp {
    private static LibraryService libraryService;
    private static Scanner scanner;
    private static NotificationThread notificationThread;
    private static final String AUTHOR_NAME_PATTERN = "^[A-Za-z][A-Za-z .'-]*$";

    public static void main(String[] args) {
        libraryService = new LibraryService();
        scanner = new Scanner(System.in);

        printWelcomeMessage();
        selectOrCreateLibrary();
        startNotificationThread();

        boolean running = true;
        while (running) {
            try {
                running = displayMenu();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Please try again.\n");
            }
        }

        stopNotificationThread();
        System.out.println("\nThank you for using Library Management System!");
        scanner.close();
    }

    private static void printWelcomeMessage() {
        System.out.println("=======================================================");
        System.out.println("     WELCOME TO LIBRARY MANAGEMENT SYSTEM");
        System.out.println("=======================================================");
        System.out.println("Manage your library efficiently with this system!");
        System.out.println("=======================================================\n");
    }

    private static void selectOrCreateLibrary() {
        while (true) {
            List<LibraryInfo> libraries = libraryService.getLibraries();

            System.out.println("=======================================");
            System.out.println("         LIBRARY SELECTION");
            System.out.println("=======================================");

            if (libraries.isEmpty()) {
                System.out.println("No existing libraries found.");
                createNewLibrary();
                return;
            }
            System.out.println("1. Open Existing Library");
            System.out.println("2. Create New Library");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice. Please enter a number.\n");
                continue;
            }

            if (choice == 1) {
                if (openExistingLibrary(libraries)) {
                    return;
                }
                continue;
            }

            if (choice == 2) {
                createNewLibrary();
                return;
            }

            System.out.println("Invalid choice. Please try again.\n");
        }
    }

    private static boolean openExistingLibrary(List<LibraryInfo> libraries) {
        System.out.println("\n=======================================");
        System.out.println("         EXISTING LIBRARIES");
        System.out.println("=======================================");

        for (int i = 0; i < libraries.size(); i++) {
            LibraryInfo info = libraries.get(i);
            String location = info.getLocation().isEmpty() ? "Unknown" : info.getLocation();
            System.out.println((i + 1) + ". " + info.getName() + " (" + location + ") - " + info.getBookCount() + " book(s)");
        }

        System.out.print("Choose a library: ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice. Please enter a number.\n");
            return false;
        }

        if (choice < 1 || choice > libraries.size()) {
            System.out.println("Invalid choice. Please try again.\n");
            return false;
        }

        LibraryInfo selected = libraries.get(choice - 1);
        libraryService.selectLibrary(selected.getName(), selected.getLocation());
        System.out.println("\nOpened library: " + selected.getName());
        System.out.println("Books available in this library: " + libraryService.getCurrentLibraryBookCount() + "\n");
        return true;
    }

    private static void createNewLibrary() {
        while (true) {
            System.out.print("Enter Library Name: ");
            String libraryName = scanner.nextLine().trim();
            if (libraryName.isEmpty()) {
                System.out.println("Library name cannot be empty.");
                continue;
            }

            System.out.print("Enter Library Location: ");
            String location = scanner.nextLine().trim();
            if (libraryService.libraryExists(libraryName, location)) {
                System.out.println("Library already exists at this location. Please choose another name/location.");
                continue;
            }
            libraryService.createLibrary(libraryName, location);
            System.out.println("\nLibrary '" + libraryName + "' (" + (location.isEmpty() ? "Unknown" : location) + ") created and opened successfully!\n");
            return;
        }
    }

    private static void startNotificationThread() {
        notificationThread = new NotificationThread();
        notificationThread.start();
        System.out.println("Background notification service started.\n");
    }

    private static void stopNotificationThread() {
        if (notificationThread != null && notificationThread.isAlive()) {
            notificationThread.stopNotifications();
            try {
                notificationThread.join(1000);
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting for notification thread.");
            }
        }
    }

    private static boolean displayMenu() {
        System.out.println("=======================================================");
        System.out.println("Current Library: " + libraryService.getCurrentLibraryName());
        System.out.println("                    MAIN MENU");
        System.out.println("=======================================================");
        System.out.println("1. Add Book");
        System.out.println("2. Issue Book");
        System.out.println("3. Return Book");
        System.out.println("4. Show All Books");
        System.out.println("5. Search Books (by Title/Author/ID)");
        System.out.println("6. Sort Books (by Title/Author/ID)");
        System.out.println("7. Display Statistics");
        System.out.println("8. Demonstrate Concurrency (Multi-threaded Issue)");
        System.out.println("9. Delete Book");
        System.out.println("10. Delete Current Library");
        System.out.println("11. Exit");
        System.out.println("=======================================================");
        System.out.print("Enter your choice: ");

        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid input! Please enter a number.");
        }

        switch (choice) {
            case 1:
                addBook();
                break;
            case 2:
                issueBook();
                break;
            case 3:
                returnBook();
                break;
            case 4:
                showAllBooks();
                break;
            case 5:
                searchBooks();
                break;
            case 6:
                sortBooks();
                break;
            case 7:
                libraryService.displayStatistics();
                break;
            case 8:
                demonstrateConcurrency();
                break;
            case 9:
                deleteBook();
                break;
            case 10:
                deleteCurrentLibrary();
                break;
            case 11:
                return false;
            default:
                throw new IllegalArgumentException("Invalid menu choice! Please select 1-11.");
        }
        return true;
    }

    private static void addBook() {
        System.out.println("\n=======================================");
        System.out.println("            ADD NEW BOOK");
        System.out.println("=======================================");

        try {
            System.out.print("Enter Book ID: ");
            String bookId = scanner.nextLine().trim();
            if (bookId.isEmpty()) {
                throw new IllegalArgumentException("Book ID cannot be empty!");
            }
            if (libraryService.searchById(bookId) != null) {
                throw new IllegalArgumentException("Book ID already exists! Duplicate not allowed in this library.");
            }

            System.out.print("Enter Title: ");
            String title = scanner.nextLine().trim();
            if (title.isEmpty()) {
                throw new IllegalArgumentException("Title cannot be empty!");
            }

            String author = readValidAuthorName();

            System.out.print("Enter Category: ");
            String category = scanner.nextLine().trim();

            System.out.print("Enter Total Copies: ");
            int totalCopies;
            try {
                totalCopies = Integer.parseInt(scanner.nextLine());
                if (totalCopies <= 0) {
                    throw new IllegalArgumentException("Total copies must be greater than 0!");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format for total copies!");
            }

            System.out.print("Is this an EBook? (y/n): ");
            String isEBook = scanner.nextLine().trim().toLowerCase();

            Book book;
            if (isEBook.equals("y") || isEBook.equals("yes")) {
                System.out.print("Enter File Format (PDF/EPUB/etc): ");
                String format = scanner.nextLine().trim();
                System.out.print("Enter File Size (MB): ");
                double size = Double.parseDouble(scanner.nextLine());
                book = new EBook(bookId, title, author, category, totalCopies, format, size);
            } else {
                book = new Book(bookId, title, author, category, totalCopies);
            }

            libraryService.addBook(book);
            System.out.println("\nBook added successfully to library '" + libraryService.getCurrentLibraryName() + "'.");
            System.out.println(book.getFormattedDetails());
            System.out.println();
        } catch (Exception e) {
            System.out.println("Error adding book: " + e.getMessage());
        }
    }

    private static void issueBook() {
        System.out.println("\n=======================================");
        System.out.println("            ISSUE BOOK");
        System.out.println("=======================================");

        try {
            System.out.print("Enter Book ID: ");
            String bookId = scanner.nextLine().trim();
            if (bookId.isEmpty()) {
                throw new IllegalArgumentException("Book ID cannot be empty!");
            }

            Book book = libraryService.searchById(bookId);
            if (book == null) {
                throw new IllegalArgumentException("Book ID not found in this library: " + bookId);
            }

            if (!book.isAvailable()) {
                System.out.println("Book is not available. Remaining copies: " + book.getRemainingCopies());
                return;
            }

            libraryService.issueBook(bookId);
            System.out.println("Borrow Duration: " + book.borrowDuration() + " days");
            System.out.println();
        } catch (Exception e) {
            System.out.println("Error issuing book: " + e.getMessage());
        }
    }

    private static void returnBook() {
        System.out.println("\n=======================================");
        System.out.println("            RETURN BOOK");
        System.out.println("=======================================");

        try {
            System.out.print("Enter Book ID: ");
            String bookId = scanner.nextLine().trim();
            if (bookId.isEmpty()) {
                throw new IllegalArgumentException("Book ID cannot be empty!");
            }

            Book book = libraryService.searchById(bookId);
            if (book == null) {
                throw new IllegalArgumentException("Book ID not found in this library: " + bookId);
            }

            libraryService.returnBook(bookId);
            System.out.println();
        } catch (Exception e) {
            System.out.println("Error returning book: " + e.getMessage());
        }
    }

    private static void deleteBook() {
        System.out.println("\n=======================================");
        System.out.println("            DELETE BOOK");
        System.out.println("=======================================");

        try {
            if (libraryService.getAllBooks().isEmpty()) {
                System.out.println("No books available to delete in this library.\n");
                return;
            }

            System.out.print("Enter Book ID to delete: ");
            String bookId = scanner.nextLine().trim();
            if (bookId.isEmpty()) {
                throw new IllegalArgumentException("Book ID cannot be empty!");
            }

            Book book = libraryService.searchById(bookId);
            if (book == null) {
                throw new IllegalArgumentException("Book ID not found in this library: " + bookId);
            }

            System.out.print("Are you sure you want to delete this book? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("y") && !confirm.equals("yes")) {
                System.out.println("Book deletion canceled.\n");
                return;
            }

            libraryService.deleteBook(bookId);
            System.out.println("Book deleted successfully from library '" + libraryService.getCurrentLibraryName() + "'.\n");
        } catch (Exception e) {
            System.out.println("Error deleting book: " + e.getMessage());
        }
    }

    private static void deleteCurrentLibrary() {
        System.out.println("\n=======================================");
        System.out.println("         DELETE CURRENT LIBRARY");
        System.out.println("=======================================");

        try {
            String libraryName = libraryService.getCurrentLibraryName();
            if (libraryName.isEmpty()) {
                System.out.println("No library is currently selected.\n");
                return;
            }

            System.out.println("Current library: " + libraryName);
            System.out.print("This will remove the library and all its books. Continue? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("y") && !confirm.equals("yes")) {
                System.out.println("Library deletion canceled.\n");
                return;
            }

            libraryService.deleteCurrentLibrary();
            System.out.println("Library deleted successfully.\n");

            // Ask user to select another existing library or create a new one.
            selectOrCreateLibrary();
        } catch (Exception e) {
            System.out.println("Error deleting library: " + e.getMessage());
        }
    }

    private static void showAllBooks() {
        libraryService.displayAllBooks();
    }

    private static String readValidAuthorName() {
        while (true) {
            System.out.print("Enter Author: ");
            String author = scanner.nextLine().trim();

            if (author.isEmpty()) {
                System.out.println("Author cannot be empty. Please enter a valid name.");
                continue;
            }

            if (!author.matches(AUTHOR_NAME_PATTERN)) {
                System.out.println("Invalid author name. Use letters, spaces, apostrophes ('), hyphens (-), and periods (.).");
                continue;
            }

            return author;
        }
    }

    private static void searchBooks() {
        System.out.println("\n=======================================");
        System.out.println("            SEARCH BOOKS");
        System.out.println("=======================================");
        System.out.println("1. Search by Title");
        System.out.println("2. Search by Author");
        System.out.println("3. Search by ID");
        System.out.print("Enter choice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            switch (choice) {
                case 1:
                    searchByTitle();
                    break;
                case 2:
                    searchByAuthor();
                    break;
                case 3:
                    searchById();
                    break;
                default:
                    System.out.println("Invalid choice! Please select 1-3.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a number.");
        }
    }

    private static void searchByTitle() {
        System.out.println("\n=======================================");
        System.out.println("         SEARCH BY TITLE");
        System.out.println("=======================================");

        System.out.print("Enter Title to search: ");
        String title = scanner.nextLine().trim();
        List<Book> results = libraryService.searchByTitle(title);

        if (results.isEmpty()) {
            System.out.println("No books found with title: " + title);
        } else {
            System.out.println("\nFound " + results.size() + " book(s):");
            results.forEach(book -> {
                System.out.println(book.getFormattedDetails());
                System.out.println();
            });
        }
    }

    private static void searchByAuthor() {
        System.out.println("\n=======================================");
        System.out.println("         SEARCH BY AUTHOR");
        System.out.println("=======================================");

        System.out.print("Enter Author name to search: ");
        String author = scanner.nextLine().trim();
        List<Book> results = libraryService.searchByAuthor(author);

        if (results.isEmpty()) {
            System.out.println("No books found by author: " + author);
        } else {
            System.out.println("\nFound " + results.size() + " book(s):");
            results.forEach(book -> {
                System.out.println(book.getFormattedDetails());
                System.out.println();
            });
        }
    }

    private static void searchById() {
        System.out.println("\n=======================================");
        System.out.println("          SEARCH BY ID");
        System.out.println("=======================================");

        System.out.print("Enter Book ID: ");
        String bookId = scanner.nextLine().trim();
        Book book = libraryService.searchById(bookId);

        if (book == null) {
            System.out.println("Book ID not found: " + bookId);
        } else {
            System.out.println("\nBook found:");
            System.out.println(book.getFormattedDetails());
            System.out.println();
        }
    }

    private static void sortBooks() {
        System.out.println("\n=======================================");
        System.out.println("            SORT BOOKS");
        System.out.println("=======================================");
        System.out.println("1. Sort by Title");
        System.out.println("2. Sort by Author");
        System.out.println("3. Sort by ID");
        System.out.print("Enter choice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());
            List<Book> sorted;

            switch (choice) {
                case 1:
                    sorted = libraryService.sortByTitle();
                    System.out.println("\nBooks sorted by Title:");
                    break;
                case 2:
                    sorted = libraryService.sortByAuthor();
                    System.out.println("\nBooks sorted by Author:");
                    break;
                case 3:
                    sorted = libraryService.sortById();
                    System.out.println("\nBooks sorted by ID:");
                    break;
                default:
                    throw new IllegalArgumentException("Invalid choice!");
            }

            sorted.forEach(book -> {
                System.out.println(book.getFormattedDetails());
                System.out.println();
            });
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a number.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void demonstrateConcurrency() {
        System.out.println("\n=======================================");
        System.out.println("    MULTI-THREADED BOOK ISSUE DEMO");
        System.out.println("=======================================");

        if (libraryService.getAllBooks().isEmpty()) {
            System.out.println("Please add at least one book first to demonstrate concurrency.");
            return;
        }

        Book demoBook = libraryService.getAllBooks().get(0);
        String bookId = demoBook.getBookId();

        if (demoBook.getTotalCopies() < 2) {
            System.out.println("Note: The demo book has only " + demoBook.getTotalCopies() + " copy. Adding more copies for demo...");
            demoBook.setTotalCopies(5);
            demoBook.setIssuedCopies(0);
        }

        System.out.println("Simulating two users issuing books simultaneously...");
        System.out.println("Book ID: " + bookId);
        System.out.println("Available copies before: " + demoBook.getRemainingCopies());
        System.out.println();

        BookIssueThread thread1 = new BookIssueThread(libraryService, bookId, "User1");
        BookIssueThread thread2 = new BookIssueThread(libraryService, bookId, "User2");

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
            System.out.println("\nAll threads completed.");
            System.out.println("Available copies after: " + demoBook.getRemainingCopies());
            System.out.println();
        } catch (InterruptedException e) {
            System.out.println("Thread execution interrupted.");
        }
    }
}

