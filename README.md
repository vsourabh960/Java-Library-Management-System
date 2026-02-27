# Library Management System

A comprehensive Java-based Library Management System that demonstrates core Java concepts including OOP principles, collections, concurrency, exception handling, and more.

## Features

### 1. Java Structure & Syntax
- ✅ `LibraryApp` class with main method
- ✅ Welcome message and user input handling
- ✅ Variable declarations and initialization
- ✅ Scanner for user input

### 2. Logic & Operations
- ✅ Arithmetic operators for tracking issued vs remaining books
- ✅ Conditional checks for book availability
- ✅ Menu-driven system using switch-case

### 3. Methods & Object Construction
- ✅ `Book` class with fields and constructor
- ✅ `issueBook()` and `returnBook()` methods
- ✅ Proper object initialization

### 4. Access Control & Structure
- ✅ Encapsulation with private fields
- ✅ Public getters and setters
- ✅ Static variable to track total number of books

### 5. String Handling
- ✅ Convert title to uppercase
- ✅ Case-insensitive title comparison
- ✅ Extract author's last name
- ✅ Formatted book details display

### 6. OOP Concepts
- ✅ `EBook` subclass extending `Book`
- ✅ Overridden `issueBook()` method in `EBook`
- ✅ `Borrowable` interface with `borrowDuration()` method
- ✅ Interface implementation in both `Book` and `EBook`

### 7. Exception Handling
- ✅ Invalid book ID handling
- ✅ Wrong menu input validation
- ✅ Duplicate book ID validation

### 8. Concurrency & Multithreading
- ✅ Background notification thread
- ✅ Multi-threaded book issue simulation
- ✅ Thread synchronization and join operations

### 9. Collections & Data Structures
- ✅ `ArrayList` for book storage
- ✅ `HashMap` for fast ID-based search
- ✅ Search by ID, title, and author
- ✅ `forEach` with Lambda expressions
- ✅ Sorting using `Comparator` (by title, author, ID)

## Project Structure

```
src/main/java/com/library/
├── LibraryApp.java              # Main application class
├── model/
│   ├── Book.java                # Book class with encapsulation
│   └── EBook.java               # EBook subclass
├── interfaces/
│   └── Borrowable.java          # Borrowable interface
├── service/
│   └── LibraryService.java      # Library operations service
└── util/
    ├── NotificationThread.java  # Background notification thread
    └── BookIssueThread.java     # Thread for concurrent book issues
```

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## How to Run

### Using Maven:

1. **Compile the project:**
   ```bash
   mvn compile
   ```

2. **Run the application:**
   ```bash
   mvn exec:java
   ```

### Using Java directly:

1. **Compile:**
   ```bash
   javac -d target/classes src/main/java/com/library/**/*.java
   ```

2. **Run:**
   ```bash
   java -cp target/classes com.library.LibraryApp
   ```

## Menu Options

1. **Add Book** - Add a new book (physical or eBook) to the library
2. **Issue Book** - Issue a book to a user
3. **Return Book** - Return a borrowed book
4. **Show All Books** - Display all books in the library
5. **Search by Title** - Find books by title (case-insensitive)
6. **Search by Author** - Find books by author name
7. **Search by ID** - Find a book by its unique ID
8. **Sort Books** - Sort books by title, author, or ID
9. **Display Statistics** - Show library statistics
10. **Demonstrate Concurrency** - Run multi-threaded book issue demo
11. **Exit** - Exit the application

## Key Concepts Demonstrated

### Encapsulation
- All fields in `Book` class are private
- Access through public getters and setters

### Inheritance
- `EBook` extends `Book`
- Method overriding in `EBook.issueBook()`

### Polymorphism
- Interface implementation (`Borrowable`)
- Different `borrowDuration()` for `Book` (14 days) and `EBook` (21 days)

### Collections
- `ArrayList<Book>` for maintaining book list
- `HashMap<String, Book>` for O(1) ID lookup
- `HashMap<String, List<Book>>` for title/author indexing

### Lambda Expressions
- `forEach` with lambda for iteration
- Stream API for filtering and searching

### Concurrency
- Background daemon thread for notifications
- Multiple threads for concurrent book operations
- Thread synchronization with `join()`

### Exception Handling
- `IllegalArgumentException` for invalid inputs
- `IllegalStateException` for unavailable books
- `NumberFormatException` for invalid number inputs

## Example Usage

```
Welcome to Library Management System
═══════════════════════════════════════════════════════

Please enter library details:
Library Name: Central Library
Library Location: New York

═══════════════════════════════════════════════════════
                    MAIN MENU
═══════════════════════════════════════════════════════
1. Add Book
2. Issue Book
...
```

## Notes

- The system validates duplicate book IDs
- EBooks can be issued to multiple users simultaneously
- Background notifications run every 10 seconds
- All threads are properly managed and joined before exit

## Author

Created as a comprehensive Java project demonstrating core Java concepts and best practices.
