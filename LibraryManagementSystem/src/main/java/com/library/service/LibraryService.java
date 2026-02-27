package com.library.service;

import com.library.model.Book;
import com.library.model.EBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for library operations.
 * Supports multiple libraries persisted in a shared JSON file.
 */
public class LibraryService {
    private static final Path DATA_FILE_PATH = Paths.get("library-data.json");
    private static final String DEFAULT_LIBRARY_NAME = "Default Library";

    private final Map<String, LibraryCatalog> libraries;
    private String currentLibraryKey;

    // Active-library indexes
    private final List<Book> books;
    private final Map<String, Book> booksById;
    private final Map<String, List<Book>> booksByTitle;
    private final Map<String, List<Book>> booksByAuthor;

    public LibraryService() {
        this.libraries = new LinkedHashMap<>();
        this.books = new ArrayList<>();
        this.booksById = new HashMap<>();
        this.booksByTitle = new HashMap<>();
        this.booksByAuthor = new HashMap<>();
        loadData();
    }

    public List<LibraryInfo> getLibraries() {
        return libraries.values().stream()
                .map(library -> new LibraryInfo(library.name, library.location, library.books.size()))
                .collect(Collectors.toList());
    }

    public boolean libraryExists(String libraryName, String location) {
        return libraries.containsKey(normalizeLibraryKey(libraryName, location));
    }

    public synchronized void createLibrary(String libraryName, String location) {
        String name = requireLibraryName(libraryName);
        String normalizedLocation = normalizeLocation(location);
        String key = normalizeLibraryKey(name, normalizedLocation);
        if (libraries.containsKey(key)) {
            throw new IllegalArgumentException("Library already exists at this location: " + name + " (" + normalizedLocation + ")");
        }

        libraries.put(key, new LibraryCatalog(name, normalizedLocation));
        selectLibrary(name, normalizedLocation);
        saveData();
    }

    public synchronized void selectLibrary(String libraryName, String location) {
        String key = normalizeLibraryKey(libraryName, location);
        LibraryCatalog catalog = libraries.get(key);
        if (catalog == null) {
            throw new IllegalArgumentException("Library not found: " + libraryName + " (" + normalizeLocation(location) + ")");
        }

        currentLibraryKey = key;
        rebuildIndexesFromCurrentLibrary();
    }

    public String getCurrentLibraryName() {
        LibraryCatalog current = getCurrentLibraryCatalog();
        return current == null ? "" : current.name;
    }

    public String getCurrentLibraryLocation() {
        LibraryCatalog current = getCurrentLibraryCatalog();
        return current == null ? "" : current.location;
    }

    public int getCurrentLibraryBookCount() {
        return books.size();
    }

    // Add a book to the current library
    public synchronized boolean addBook(Book book) {
        ensureLibrarySelected();

        if (booksById.containsKey(book.getBookId())) {
            throw new IllegalArgumentException("Book ID already exists in this library: " + book.getBookId());
        }

        LibraryCatalog catalog = getCurrentLibraryCatalog();
        catalog.books.add(book);
        addBookToIndexes(book);
        saveData();
        return true;
    }

    public synchronized boolean issueBook(String bookId) {
        ensureLibrarySelected();

        Book book = booksById.get(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book ID not found: " + bookId);
        }
        if (!book.isAvailable()) {
            throw new IllegalStateException("Book is not available for issue.");
        }
        book.issueBook();
        saveData();
        return true;
    }

    public synchronized boolean returnBook(String bookId) {
        ensureLibrarySelected();

        Book book = booksById.get(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book ID not found: " + bookId);
        }
        book.returnBook();
        saveData();
        return true;
    }

    public synchronized boolean deleteBook(String bookId) {
        ensureLibrarySelected();

        Book existing = booksById.get(bookId);
        if (existing == null) {
            throw new IllegalArgumentException("Book ID not found: " + bookId);
        }

        LibraryCatalog catalog = getCurrentLibraryCatalog();
        catalog.books.removeIf(book -> book.getBookId().equals(bookId));
        rebuildIndexesFromCurrentLibrary();
        saveData();
        return true;
    }

    public synchronized void deleteCurrentLibrary() {
        ensureLibrarySelected();

        String removedKey = currentLibraryKey;
        libraries.remove(removedKey);

        if (libraries.isEmpty()) {
            currentLibraryKey = null;
            books.clear();
            booksById.clear();
            booksByTitle.clear();
            booksByAuthor.clear();
        } else {
            currentLibraryKey = libraries.keySet().iterator().next();
            rebuildIndexesFromCurrentLibrary();
        }

        saveData();
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public Book searchById(String bookId) {
        return booksById.get(bookId);
    }

    public List<Book> searchByTitle(String title) {
        String titleKey = title.toLowerCase();
        List<Book> foundBooks = booksByTitle.get(titleKey);
        if (foundBooks == null) {
            return books.stream()
                    .filter(book -> book.compareTitleIgnoreCase(title))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>(foundBooks);
    }

    public List<Book> searchByAuthor(String author) {
        String authorKey = author.toLowerCase();
        List<Book> foundBooks = booksByAuthor.get(authorKey);
        if (foundBooks == null) {
            return books.stream()
                    .filter(book -> book.getAuthor().equalsIgnoreCase(author))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>(foundBooks);
    }

    public List<Book> sortByTitle() {
        List<Book> sorted = new ArrayList<>(books);
        sorted.sort(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }

    public List<Book> sortByAuthor() {
        List<Book> sorted = new ArrayList<>(books);
        sorted.sort(Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }

    public List<Book> sortById() {
        List<Book> sorted = new ArrayList<>(books);
        sorted.sort(Comparator.comparing(Book::getBookId));
        return sorted;
    }

    public void displayAllBooks() {
        System.out.println("\n=======================================================");
        System.out.println("Library: " + getCurrentLibraryName());
        System.out.println("                 ALL BOOKS IN LIBRARY");
        System.out.println("=======================================================");

        if (books.isEmpty()) {
            System.out.println("No books in the library.");
            return;
        }

        books.forEach(book -> {
            System.out.println(book.getFormattedDetails());
            System.out.println();
        });

        System.out.println("Books in Current Library: " + books.size());
        System.out.println("=======================================================\n");
    }

    public void displayStatistics() {
        long totalBooks = books.size();
        long availableBooks = books.stream()
                .filter(Book::isAvailable)
                .count();
        long totalIssued = books.stream()
                .mapToInt(Book::getIssuedCopies)
                .sum();

        System.out.println("\n=======================================");
        System.out.println("Library: " + getCurrentLibraryName());
        System.out.println("           LIBRARY STATISTICS");
        System.out.println("=======================================");
        System.out.println("Total Books: " + totalBooks);
        System.out.println("Available Books: " + availableBooks);
        System.out.println("Total Issued Copies: " + totalIssued);
        System.out.println("=======================================\n");
    }

    private void ensureLibrarySelected() {
        if (getCurrentLibraryCatalog() == null) {
            throw new IllegalStateException("No library selected.");
        }
    }

    private void rebuildIndexesFromCurrentLibrary() {
        books.clear();
        booksById.clear();
        booksByTitle.clear();
        booksByAuthor.clear();

        LibraryCatalog current = getCurrentLibraryCatalog();
        if (current == null) {
            return;
        }

        for (Book book : current.books) {
            addBookToIndexes(book);
        }
    }

    private LibraryCatalog getCurrentLibraryCatalog() {
        if (currentLibraryKey == null) {
            return null;
        }
        return libraries.get(currentLibraryKey);
    }

    private void addBookToIndexes(Book book) {
        books.add(book);
        booksById.put(book.getBookId(), book);

        String titleKey = book.getTitle().toLowerCase();
        booksByTitle.computeIfAbsent(titleKey, k -> new ArrayList<>()).add(book);

        String authorKey = book.getAuthor().toLowerCase();
        booksByAuthor.computeIfAbsent(authorKey, k -> new ArrayList<>()).add(book);
    }

    private void saveData() {
        try {
            Files.writeString(DATA_FILE_PATH, toJsonArray());
        } catch (IOException e) {
            System.out.println("Warning: Unable to save library data. " + e.getMessage());
        }
    }

    private void loadData() {
        if (!Files.exists(DATA_FILE_PATH)) {
            return;
        }

        try {
            String json = Files.readString(DATA_FILE_PATH);
            List<Map<String, String>> rows = parseJsonArray(json);

            for (Map<String, String> row : rows) {
                String recordType = defaultString(row.get("recordType"));

                if ("LIBRARY".equalsIgnoreCase(recordType)) {
                    String libraryName = requireLibraryName(row.get("libraryName"));
                    String libraryLocation = normalizeLocation(row.get("libraryLocation"));
                    ensureLibraryCatalog(libraryName, libraryLocation);
                    continue;
                }

                String libraryName = defaultString(row.get("libraryName"));
                if (libraryName.isEmpty()) {
                    libraryName = DEFAULT_LIBRARY_NAME;
                }
                String libraryLocation = normalizeLocation(row.get("libraryLocation"));
                LibraryCatalog catalog = ensureLibraryCatalog(libraryName, libraryLocation);

                Book book = fromBookRow(row);
                if (book == null) {
                    continue;
                }
                if (containsBookId(catalog.books, book.getBookId())) {
                    continue;
                }
                catalog.books.add(book);
            }

            if (!libraries.isEmpty()) {
                String firstKey = libraries.keySet().iterator().next();
                currentLibraryKey = firstKey;
                rebuildIndexesFromCurrentLibrary();
            }
        } catch (Exception e) {
            System.out.println("Warning: Unable to load existing library data. " + e.getMessage());
        }
    }

    private boolean containsBookId(List<Book> existingBooks, String bookId) {
        for (Book book : existingBooks) {
            if (book.getBookId().equals(bookId)) {
                return true;
            }
        }
        return false;
    }

    private LibraryCatalog ensureLibraryCatalog(String libraryName, String location) {
        String name = requireLibraryName(libraryName);
        String normalizedLocation = normalizeLocation(location);
        String key = normalizeLibraryKey(name, normalizedLocation);
        LibraryCatalog existing = libraries.get(key);
        if (existing != null) {
            return existing;
        }

        LibraryCatalog created = new LibraryCatalog(name, normalizedLocation);
        libraries.put(key, created);
        return created;
    }

    private String toJsonArray() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        boolean firstRecord = true;

        for (LibraryCatalog catalog : libraries.values()) {
            if (!firstRecord) {
                sb.append(",\n");
            }
            firstRecord = false;
            sb.append("  {\n");
            appendStringField(sb, "recordType", "LIBRARY", true);
            appendStringField(sb, "libraryName", catalog.name, true);
            appendStringField(sb, "libraryLocation", catalog.location, false);
            sb.append("  }");

            for (Book book : catalog.books) {
                sb.append(",\n");
                boolean isEbook = book instanceof EBook;
                sb.append("  {\n");
                appendStringField(sb, "recordType", "BOOK", true);
                appendStringField(sb, "libraryName", catalog.name, true);
                appendStringField(sb, "libraryLocation", catalog.location, true);
                appendStringField(sb, "type", isEbook ? "EBOOK" : "BOOK", true);
                appendStringField(sb, "bookId", book.getBookId(), true);
                appendStringField(sb, "title", book.getTitle(), true);
                appendStringField(sb, "author", book.getAuthor(), true);
                appendStringField(sb, "category", book.getCategory(), true);
                appendNumberField(sb, "totalCopies", String.valueOf(book.getTotalCopies()), true);
                appendNumberField(sb, "issuedCopies", String.valueOf(book.getIssuedCopies()), true);

                if (isEbook) {
                    EBook eBook = (EBook) book;
                    appendStringField(sb, "fileFormat", eBook.getFileFormat(), true);
                    appendNumberField(sb, "fileSizeMB", String.valueOf(eBook.getFileSizeMB()), false);
                } else {
                    appendStringField(sb, "fileFormat", "", true);
                    appendNumberField(sb, "fileSizeMB", "0.0", false);
                }
                sb.append("  }");
            }
        }

        sb.append("\n]");
        return sb.toString();
    }

    private void appendStringField(StringBuilder sb, String key, String value, boolean comma) {
        sb.append("    \"").append(key).append("\": \"").append(escapeJson(value)).append("\"");
        if (comma) {
            sb.append(",");
        }
        sb.append("\n");
    }

    private void appendNumberField(StringBuilder sb, String key, String value, boolean comma) {
        sb.append("    \"").append(key).append("\": ").append(value);
        if (comma) {
            sb.append(",");
        }
        sb.append("\n");
    }

    private List<Map<String, String>> parseJsonArray(String json) {
        List<Map<String, String>> result = new ArrayList<>();
        List<String> objectJsonList = splitTopLevelObjects(json);
        for (String objectJson : objectJsonList) {
            result.add(parseObject(objectJson));
        }
        return result;
    }

    private List<String> splitTopLevelObjects(String json) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int objectStart = -1;
        boolean inString = false;
        boolean escaping = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (inString) {
                if (escaping) {
                    escaping = false;
                } else if (c == '\\') {
                    escaping = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                if (depth == 0) {
                    objectStart = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objectStart >= 0) {
                    objects.add(json.substring(objectStart, i + 1));
                    objectStart = -1;
                }
            }
        }

        return objects;
    }

    private Map<String, String> parseObject(String objectJson) {
        Map<String, String> map = new LinkedHashMap<>();
        int index = 0;

        while (index < objectJson.length()) {
            int keyStart = objectJson.indexOf('"', index);
            if (keyStart < 0) {
                break;
            }
            ParsedString key = parseQuotedString(objectJson, keyStart);
            if (key == null) {
                break;
            }

            int colon = objectJson.indexOf(':', key.nextIndex);
            if (colon < 0) {
                break;
            }

            int valueStart = colon + 1;
            while (valueStart < objectJson.length() && Character.isWhitespace(objectJson.charAt(valueStart))) {
                valueStart++;
            }

            String value;
            int nextIndex;
            if (valueStart < objectJson.length() && objectJson.charAt(valueStart) == '"') {
                ParsedString parsedValue = parseQuotedString(objectJson, valueStart);
                if (parsedValue == null) {
                    break;
                }
                value = parsedValue.value;
                nextIndex = parsedValue.nextIndex;
            } else {
                int comma = objectJson.indexOf(',', valueStart);
                int endBrace = objectJson.indexOf('}', valueStart);
                int valueEnd;
                if (comma < 0) {
                    valueEnd = endBrace;
                } else if (endBrace < 0) {
                    valueEnd = comma;
                } else {
                    valueEnd = Math.min(comma, endBrace);
                }
                if (valueEnd < 0) {
                    valueEnd = objectJson.length();
                }
                value = objectJson.substring(valueStart, valueEnd).trim();
                nextIndex = valueEnd + 1;
            }

            map.put(key.value, value);
            index = nextIndex;
        }

        return map;
    }

    private ParsedString parseQuotedString(String text, int startQuote) {
        if (startQuote < 0 || startQuote >= text.length() || text.charAt(startQuote) != '"') {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean escaping = false;

        for (int i = startQuote + 1; i < text.length(); i++) {
            char c = text.charAt(i);

            if (escaping) {
                switch (c) {
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    default:
                        sb.append(c);
                }
                escaping = false;
                continue;
            }

            if (c == '\\') {
                escaping = true;
            } else if (c == '"') {
                return new ParsedString(sb.toString(), i + 1);
            } else {
                sb.append(c);
            }
        }

        return null;
    }

    private Book fromBookRow(Map<String, String> row) {
        String bookId = row.get("bookId");
        if (bookId == null || bookId.trim().isEmpty()) {
            return null;
        }

        int totalCopies = parseInt(row.get("totalCopies"), 1);
        int issuedCopies = parseInt(row.get("issuedCopies"), 0);
        if (totalCopies < 1) {
            totalCopies = 1;
        }
        if (issuedCopies < 0) {
            issuedCopies = 0;
        }
        if (issuedCopies > totalCopies) {
            issuedCopies = totalCopies;
        }

        String type = row.get("type");
        Book book;
        if ("EBOOK".equalsIgnoreCase(type)) {
            book = new EBook(
                    bookId,
                    defaultString(row.get("title")),
                    defaultString(row.get("author")),
                    defaultString(row.get("category")),
                    totalCopies,
                    defaultString(row.get("fileFormat")),
                    parseDouble(row.get("fileSizeMB"), 0.0)
            );
        } else {
            book = new Book(
                    bookId,
                    defaultString(row.get("title")),
                    defaultString(row.get("author")),
                    defaultString(row.get("category")),
                    totalCopies
            );
        }

        book.setIssuedCopies(issuedCopies);
        return book;
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private String requireLibraryName(String value) {
        String name = defaultString(value).trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Library name cannot be empty.");
        }
        return name;
    }

    private String normalizeLibraryKey(String name, String location) {
        return defaultString(name).trim().toLowerCase() + "::" + normalizeLocation(location).toLowerCase();
    }

    private String normalizeLocation(String location) {
        String normalized = defaultString(location).trim();
        return normalized.isEmpty() ? "Unknown" : normalized;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
