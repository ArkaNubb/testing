package Main;

import book.Book;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import user.Member;

import java.io.IOException;

public class MemberController {

    private Member currentMember;

    @FXML private Label welcomeLabel;
    @FXML private Label borrowMessageLabel;
    @FXML private Label returnMessageLabel;

    @FXML private TableView<Book> allBooksTable;
    @FXML private TableColumn<Book, String> allBookTitleCol;
    @FXML private TableColumn<Book, String> allBookAuthorCol;
    @FXML private TableColumn<Book, String> allBookIdCol;
    @FXML private TableColumn<Book, Number> allBookAvailableCol;
    @FXML private TextField borrowBookIdField;

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
        allBookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        allBookAuthorCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthorName()));
        allBookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
        allBookAvailableCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAvailable_copies()));

        myBookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        myBookAuthorCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthorName()));
        myBookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
    }

    private void loadData() {
        // This requires the getAllBooks() method you will add to BookService
        allBooksTable.setItems(FXCollections.observableArrayList(Main.bookService.getAllBooks()));
        if (currentMember != null) {
            // This requires the getBorrowedBooks() method you will add to Member
            myBooksTable.setItems(FXCollections.observableArrayList(currentMember.getBorrowedBooks()));
        }
    }

    @FXML void handleRequestBorrow(ActionEvent event) {
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

    @FXML void handleRequestReturn(ActionEvent event) {
        String bookId = returnBookIdField.getText();
        String ratingStr = ratingField.getText();
        if (bookId.isEmpty()) {
            returnMessageLabel.setText("Please enter a Book ID.");
            return;
        }
        if (ratingStr.isEmpty()) {
            ratingStr = "0.0";
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

    @FXML void handleLogout(ActionEvent event) throws IOException {
        SceneManager.setCurrentUser(null);
        SceneManager.switchScene("/Main/login-view.fxml");
    }
}