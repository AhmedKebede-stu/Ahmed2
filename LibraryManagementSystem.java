import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

// Interface for library items
interface LibraryItem {
    void displayInfo();
    String getTitle();
    String getUniqueId();
}

// Abstract class representing a general item in the library
abstract class Item implements LibraryItem {
    protected String id;
    protected String title;
    protected boolean isAvailable;

    public Item(String id, String title) {
        this.id = id;
        this.title = title;
        this.isAvailable = true;
    }

    public String getTitle() {
        return title;
    }

    public String getUniqueId() {
        return id;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public abstract void displayInfo();
}

// Book class (inheritance)
class Book extends Item {
    private String author;
    private int publicationYear;

    public Book(String id, String title, String author, int publicationYear) {
        super(id, title);
        this.author = author;
        this.publicationYear = publicationYear;
    }

    @Override
    public void displayInfo() {
        System.out.println("Book ID: " + id);
        System.out.println("Title: " + title);
        System.out.println("Author: " + author);
        System.out.println("Publication Year: " + publicationYear);
        System.out.println("Status: " + (isAvailable ? "Available" : "Borrowed"));
    }
}

// DVD class (inheritance)
class DVD extends Item {
    private String director;
    private int duration; // in minutes

    public DVD(String id, String title, String director, int duration) {
        super(id, title);
        this.director = director;
        this.duration = duration;
    }

   // @Override
    public void displayInfo() {
        System.out.println("DVD ID: " + id);
        System.out.println("Title: " + title);
        System.out.println("Director: " + director);
        System.out.println("Duration: " + duration + " minutes");
        System.out.println("Status: " + (isAvailable ? "Available" : "Borrowed"));
    }
}

// User class (encapsulation)
class User {
    private String userId;
    private String name;
    private List<String> borrowedItems;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.borrowedItems = new ArrayList<>();
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public List<String> getBorrowedItems() {
        return Collections.unmodifiableList(borrowedItems);
    }

    // Methods to manage borrowed items
    public void borrowItem(String itemId) {
        borrowedItems.add(itemId);
    }

    public boolean returnItem(String itemId) {
        return borrowedItems.remove(itemId);
    }

    public void displayUserInfo() {
        System.out.println("User ID: " + userId);
        System.out.println("Name: " + name);
        System.out.println("Borrowed Items: " + borrowedItems.size());
    }
}

// Custom exception for library operations
class LibraryException extends Exception {
    public LibraryException(String message) {
        super(message);
    }
}

// Library class (main class that manages everything)
class Library {
    private Map<String, Item> items;
    private Map<String, User> users;
    private Map<String, String> borrowRecords; // itemId to userId
    private String dataFile;

    public Library(String dataFile) {
        this.items = new HashMap<>();
        this.users = new HashMap<>();
        this.borrowRecords = new HashMap<>();
        this.dataFile = dataFile;
        loadData();
    }

    // Add items to library (polymorphism through method overloading)
    public void addItem(Book book) {
        items.put(book.getUniqueId(), book);
    }

    public void addItem(DVD dvd) {
        items.put(dvd.getUniqueId(), dvd);
    }

    // Register user
    public void registerUser(User user) throws LibraryException {
        if (users.containsKey(user.getUserId())) {
            throw new LibraryException("User ID already exists");
        }
        users.put(user.getUserId(), user);
    }

    // Borrow item
    public void borrowItem(String userId, String itemId) throws LibraryException {
        if (!users.containsKey(userId)) {
            throw new LibraryException("User not found");
        }

        if (!items.containsKey(itemId)) {
            throw new LibraryException("Item not found");
        }

        Item item = items.get(itemId);
        if (!item.isAvailable()) {
            throw new LibraryException("Item is already borrowed");
        }

        User user = users.get(userId);
        if (user.getBorrowedItems().size() >= 5) {
            throw new LibraryException("User has reached maximum borrowing limit");
        }

        item.setAvailable(false);
        user.borrowItem(itemId);
        borrowRecords.put(itemId, userId);
    }

    // Return item
    public void returnItem(String itemId) throws LibraryException {
        if (!items.containsKey(itemId)) {
            throw new LibraryException("Item not found");
        }

        if (!borrowRecords.containsKey(itemId)) {
            throw new LibraryException("Item was not borrowed");
        }

        Item item = items.get(itemId);
        String userId = borrowRecords.get(itemId);
        User user = users.get(userId);

        item.setAvailable(true);
        user.returnItem(itemId);
        borrowRecords.remove(itemId);
    }

    // Display all items (polymorphism through interface)
    public void displayAllItems() {
        System.out.println("\nLibrary Items:");
        for (Item item : items.values()) {
            item.displayInfo();
            System.out.println();
        }
    }

    // Display all users
    public void displayAllUsers() {
        System.out.println("\nLibrary Users:");
        for (User user : users.values()) {
            user.displayUserInfo();
            System.out.println();
        }
    }

    // Search items by title
    public void searchItemsByTitle(String title) {
        System.out.println("\nSearch Results for '" + title + "':");
        boolean found = false;
        for (Item item : items.values()) {
            if (item.getTitle().toLowerCase().contains(title.toLowerCase())) {
                item.displayInfo();
                System.out.println();
                found = true;
            }
        }
        if (!found) {
            System.out.println("No items found with the given title.");
        }
    }

    // File management - save data to file
    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            oos.writeObject(items);
            oos.writeObject(users);
            oos.writeObject(borrowRecords);
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    // File management - load data from file
   @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(dataFile);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
                items = (Map<String, Item>) ois.readObject();
                users = (Map<String, User>) ois.readObject();
                borrowRecords = (Map<String, String>) ois.readObject();
                System.out.println("Data loaded successfully.");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading data: " + e.getMessage());
            }
        }
    }
}

// Main class to run the application
public class LibraryManagementSystem {
    public static void main(String[] args) {
        Library library = new Library("library_data.ser");
        Scanner scanner = new Scanner(System.in);

        // Sample data initialization
        initializeSampleData(library);

        while (true) {
            System.out.println("\t\t\tWOLDIA UNIVERSITY\n\t\t\tSCHOOL OF COMPUTING\n\t\t\t DEPARTMENT OF SOFTWARE ENGINEERIG\n");
            System.out.println("\t\t\t0bject oreinted  PROGRAMMING\n\tGROUP 3  project");
            System.out.println("\n  Title     Library Management System");
            System.out.println( "\tName of Group Members\t\tID Number");
            System.out.println("\t1.  Gebremikaeil  Aweta\t\tWDU160578");
            System.out.println( "\t2.  Ahmed   Kebede  \t\tWDU160089 ");
            System.out.println("\t3.  Mikael   Alemayehu\t\tWDU160938");
            System.out.println( "\t4.  Mahder   Azmeraw\t\tWDU160803");
            System.out.println( "\t5.  Samrawit   Amha\t\tWDU161056");
            System.out.println("\t6.  Zyyeich   Wuletaw\t\tWDU161397");
            
            System.out.println("\t\t\t\t\t\t\t\t\t\t\t SUMITTED TO : MR Melese E");
            System.out.println("\t\t\t\t\t\t\t\t\t\t\tSUMITTED DATE : 27/09/2017");
            System.out.println("1. Add Book");
            System.out.println("2. Add DVD");
            System.out.println("3. Register User");
            System.out.println("4. Borrow Item");
            System.out.println("5. Return Item");
            System.out.println("6. Display All Items");
            System.out.println("7. Display All Users");
            System.out.println("8. Search Items by Title");
            System.out.println("9. Save Data");
            System.out.println("10. Exit");
            System.out.print("Enter your choice: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1:
                        addBook(library, scanner);
                        break;
                    case 2:
                        addDVD(library, scanner);
                        break;
                    case 3:
                        registerUser(library, scanner);
                        break;
                    case 4:
                        borrowItem(library, scanner);
                        break;
                    case 5:
                        returnItem(library, scanner);
                        break;
                    case 6:
                        library.displayAllItems();
                        break;
                    case 7:
                        library.displayAllUsers();
                        break;
                    case 8:
                        System.out.print("Enter title to search: ");
                        String title = scanner.nextLine();
                        library.searchItemsByTitle(title);
                        break;
                    case 9:
                        library.saveData();
                        break;
                    case 10:
                        System.out.println("Exiting...");
                        scanner.close();
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // clear the invalid input
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    private static void initializeSampleData(Library library) {
        try {
            // Add sample books
            library.addItem(new Book("B001", "Java Programming", "eden gugsa", 2020));
            library.addItem(new Book("B002", "Python Basics", "Jane Smith", 2019));

            // Add sample DVDs
            library.addItem(new DVD("D001", "The Matrix", "Lana Wachowski", 136));
            library.addItem(new DVD("D002", "Inception", "Christopher Nolan", 148));

            // Register sample users
            library.registerUser(new User("U001", "Alice Johnson"));
            library.registerUser(new User("U002", "Bob Williams"));
        } catch (LibraryException e) {
            System.out.println("Error initializing sample data: " + e.getMessage());
        }
    }

    private static void addBook(Library library, Scanner scanner) throws LibraryException {
        System.out.print("Enter Book ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Title: ");
        String title = scanner.nextLine();
        System.out.print("Enter Author: ");
        String author = scanner.nextLine();
        System.out.print("Enter Publication Year: ");
        int year = scanner.nextInt();
        scanner.nextLine(); // consume newline

        library.addItem(new Book(id, title, author, year));
        System.out.println("Book added successfully.");
    }

    private static void addDVD(Library library, Scanner scanner) throws LibraryException {
        System.out.print("Enter DVD ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Title: ");
        String title = scanner.nextLine();
        System.out.print("Enter Director: ");
        String director = scanner.nextLine();
        System.out.print("Enter Duration (minutes): ");
        int duration = scanner.nextInt();
        scanner.nextLine(); // consume newline

        library.addItem(new DVD(id, title, director, duration));
        System.out.println("DVD added successfully.");
    }

    private static void registerUser(Library library, Scanner scanner) throws LibraryException {
        System.out.print("Enter User ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();

        library.registerUser(new User(id, name));
        System.out.println("User registered successfully.");
    }

    private static void borrowItem(Library library, Scanner scanner) throws LibraryException {
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine();
        System.out.print("Enter Item ID: ");
        String itemId = scanner.nextLine();

        library.borrowItem(userId, itemId);
        System.out.println("Item borrowed successfully.");
    }

    private static void returnItem(Library library, Scanner scanner) throws LibraryException {
        System.out.print("Enter Item ID to return: ");
        String itemId = scanner.nextLine();

        library.returnItem(itemId);
        System.out.println("Item returned successfully.");
    }
}