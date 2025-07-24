// Place in package: Main
// This is the main entry point for the JavaFX application.
package Main;

import book.Book;
import service.AuthorService;
import service.BookService;
import service.LibrarianService;
import service.MemberService;
import user.Author;
import user.Librarian;
import user.Member;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends Application {

    // Static services to be accessible from all UI controllers
    public static BookService bookService;
    public static MemberService memberService;
    public static AuthorService authorService;
    public static LibrarianService librarianService;
    public static Librarian currentLibrarian;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // The SceneManager will handle all navigation between different screens
        SceneManager.setStage(primaryStage);

        // Load the initial login view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main/login-view.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Library Management System");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) throws IOException {
        // --- DATA LOADING ---
        // This logic is taken from your original main method and adapted slightly.

        // IMPORTANT: Ensure these file paths are correct for your project structure.
        // Using relative paths from the project root is recommended.
        String bookInfoPath = "src/main/java/Main/bookInformation.txt";
        String authorInfoPath = "src/main/java/Main/authorInformation.txt";
        String memberInfoPath = "src/main/java/Main/memberInformation.txt";
        String librarianInfoPath = "src/main/java/Main/librarianInformation.txt";

        // --- Reading Data from Files ---
        List<String> bookList = Files.readAllLines(Paths.get(bookInfoPath));
        List<String> authorList = Files.readAllLines(Paths.get(authorInfoPath));
        List<String> memberList = Files.readAllLines(Paths.get(memberInfoPath));
        String librarianData = Files.readAllLines(Paths.get(librarianInfoPath)).get(0);


        // --- SERVICE INITIALIZATION ---
        List<Book> allBooks = new ArrayList<>();
        for (String bookInfo : bookList) {
            if (bookInfo.trim().isEmpty()) continue;
            String[] values = bookInfo.split("\\|");
            List<String> genre = Arrays.stream(values[4].split(",")).map(String::trim).collect(Collectors.toList());
            List<Double> ratings = new ArrayList<>();
            if (!values[5].equalsIgnoreCase("dummyrating") && !values[5].trim().isEmpty()) {
                for (String r : values[5].split(",")) {
                    if(!r.trim().isEmpty()) ratings.add(Double.parseDouble(r.trim()));
                }
            }
            int total_copies = Integer.parseInt(values[6].trim());
            int available_copies = Integer.parseInt(values[7].trim());
            Book book = new Book(values[0], values[1], values[2], values[3], genre, ratings, total_copies, available_copies);
            allBooks.add(book);
        }
        bookService = new BookService(allBooks);

        List<Author> allAuthors = new ArrayList<>();
        for (String authorInfo : authorList) {
            if (authorInfo.trim().isEmpty()) continue;
            String[] values = authorInfo.split("\\|");
            List<Book> authorPublishedBooks = new ArrayList<>();
            if(!values[4].equalsIgnoreCase("dummybook")){
                String[] bookIds = values[4].split(",");
                for (String bookId : bookIds) {
                    if(!bookId.trim().isEmpty()) {
                        Book foundBook = BookService.findBook(bookId.trim());
                        if (foundBook != null) authorPublishedBooks.add(foundBook);
                    }
                }
            }
            Author author = new Author(values[0], values[1], values[2], values[3], authorPublishedBooks);
            allAuthors.add(author);
        }
        authorService = new AuthorService(allAuthors);

        List<Member> allMembers = new ArrayList<>();
        for (String memberInfo : memberList) {
            if (memberInfo.trim().isEmpty()) continue;
            String[] values = memberInfo.split("\\|");
            List<Book> booklist = new ArrayList<>();
            if(!values[4].equalsIgnoreCase("dummybook")){
                String[] bookIds = values[4].split(",");
                for (String bookId : bookIds) {
                    if(!bookId.trim().isEmpty()) {
                        Book foundBook = BookService.findBook(bookId.trim());
                        if (foundBook != null) booklist.add(foundBook);
                    }
                }
            }
            Member member = new Member(values[0], values[1], values[2], values[3], booklist);
            allMembers.add(member);
        }
        memberService = new MemberService(allMembers);

        String[] values = librarianData.split("\\|");
        currentLibrarian = new Librarian(values[0], values[1], values[2], values[3]);
        librarianService = new LibrarianService(currentLibrarian);

        // Launch the JavaFX application
        launch(args);
    }
}