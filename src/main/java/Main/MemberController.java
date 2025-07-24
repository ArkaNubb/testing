package Main;

import book.Book;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import user.Member;

import java.io.IOException;

public class MemberController {

    private Member currentMember;

    @FXML private Button logoutButton;

    @FXML private Label welcomeLabel;
    @FXML private Label borrowMessageLabel;
    @FXML private Label returnMessageLabel;

    // All Books Table
    @FXML private TableView<Book> allBooksTable;
    @FXML private TableColumn<Book, String> allBookTitleCol;
    @FXML private TableColumn<Book, String> allBookAuthorCol;
    @FXML private TableColumn<Book, String> allBookIdCol;
    @FXML private TableColumn<Book, Number> allBookAvailableCol;
    @FXML private TextField borrowBookIdField;

    // My Books Table
    @FXML private TableView<Book> myBooksTable;
    @FXML private TableColumn<Book, String> myBookTitleCol;
    @FXML private TableColumn<Book, String> myBookAuthorCol;
    @FXML private TableColumn<Book, String> myBookIdCol;
    @FXML private TextField returnBookIdField;
    @FXML private TextField ratingField;

    @FXML
    public void initialize() {
        currentMember = (Member) SceneManager.getCurrentUser();
        if (currentMember != null) {
            welcomeLabel.setText("Welcome, " + currentMember.getName());
        }

        setupTables();
        loadData();
    }

    private void setupTables() {
        // All Books: Use callbacks to get data from standard getters
        allBookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        allBookAuthorCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthorName()));
        allBookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
        allBookAvailableCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAvailable_copies()));

        // My Books
        myBookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        myBookAuthorCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthorName()));
        myBookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
    }

    private void loadData() {
        allBooksTable.setItems(FXCollections.observableArrayList(Main.bookService.getAllBooks()));
        if (currentMember != null) {
            myBooksTable.setItems(FXCollections.observableArrayList(currentMember.getBorrowedBooks()));
        }
    }

    @FXML
    void handleRequestBorrow(ActionEvent event) {
        String bookId = borrowBookIdField.getText();
        if (bookId.isEmpty()) {
            borrowMessageLabel.setText("Please enter a Book ID.");
            return;
        }

        if (!currentMember.isMemberCanBorrow()) {
            borrowMessageLabel.setText("You have reached the borrow limit (5 books).");
            return;
        }

        try {
            Main.librarianService.requestBorrowedBook(currentMember, bookId);
            borrowMessageLabel.setText("Borrow request sent for Book ID: " + bookId);
            borrowBookIdField.clear();
        } catch (IOException e) {
            borrowMessageLabel.setText("Error sending request.");
            e.printStackTrace();
        }
    }

    @FXML
    void handleRequestReturn(ActionEvent event) {
        String bookId = returnBookIdField.getText();
        String ratingStr = ratingField.getText();
        if (bookId.isEmpty()) {
            returnMessageLabel.setText("Please enter a Book ID.");
            return;
        }
        if (ratingStr.isEmpty()) {
            ratingStr = "0.0"; // Default rating if none is provided
        }

        try {
            double rating = Double.parseDouble(ratingStr);
            if (rating < 0 || rating > 5) {
                returnMessageLabel.setText("Rating must be between 0 and 5.");
                return;
            }
            Main.librarianService.returnBorrowedBook(currentMember, bookId, rating);
            returnMessageLabel.setText("Return request sent for Book ID: " + bookId);
            returnBookIdField.clear();
            ratingField.clear();
        } catch (NumberFormatException e) {
            returnMessageLabel.setText("Invalid rating. Please enter a number.");
        } catch (IOException e) {
            returnMessageLabel.setText("Error sending request.");
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(ActionEvent event) throws IOException {
        // Clear the currently logged-in user's data
        SceneManager.setCurrentUser(null);

        // Switch the scene back to the login view
        SceneManager.switchScene("/Main/login-view.fxml");
    }
}