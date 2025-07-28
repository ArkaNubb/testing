package Main;

import common.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import service.server;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MemberController implements Initializable {

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

    private final ObservableList<Book> allBooksData = FXCollections.observableArrayList();
    private final ObservableList<Book> myBooksData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentMember = (Member) SceneManager.getCurrentUser();
        if (currentMember != null) {
            welcomeLabel.setText("Welcome, " + currentMember.getName());
        }
        setupTables();

        // Set the controller instance in Main for real-time updates
        Main.setMemberController(this);

        // Load initial data
        loadData();
    }

    private void setupTables() {
        // Setup cell value factories
        allBookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        allBookAuthorCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthorName()));
        allBookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
        allBookAvailableCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAvailable_copies()));

        myBookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        myBookAuthorCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthorName()));
        myBookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));

        // Set the ObservableList to tables (do this once)
        allBooksTable.setItems(allBooksData);
        myBooksTable.setItems(myBooksData);
    }

    public void loadData() {
        Platform.runLater(() -> {
            System.out.println("=== Refreshing Member UI ===");

            MemberPackage memberPackage = Main.getMemberPackage();
            if (memberPackage == null) {
                System.out.println("ERROR: Member package is null. Cannot load data.");
                return;
            }

            // Clear previous data
            allBooksData.clear();
            myBooksData.clear();

            // Update all books data
            if (memberPackage.getAllBooks() != null) {
                allBooksData.addAll(memberPackage.getAllBooks());
                System.out.println("Loaded " + allBooksData.size() + " books in all books table");
            }

            // Update member's borrowed books
            Member updatedMember = memberPackage.getMember();
            if (updatedMember != null && updatedMember.getBorrowedBooks() != null) {
                myBooksData.addAll(updatedMember.getBorrowedBooks());
                System.out.println("Loaded " + myBooksData.size() + " borrowed books for member");

                // Update the current member reference
                currentMember = updatedMember;
            }

            // Force table refresh
            allBooksTable.refresh();
            myBooksTable.refresh();

            System.out.println("=== Member UI Update Complete ===");
        });
    }


    @FXML void handleRequestBorrow(ActionEvent event) throws IOException {
        String bookId = borrowBookIdField.getText();
        if (bookId.isEmpty()) {
            borrowMessageLabel.setText("Please enter a Book ID.");
            return;
        }
        if (!currentMember.isMemberCanBorrow()) {
            borrowMessageLabel.setText("You have reached the borrow limit (5 books).");
            return;
        }
        SocketWrapper socketWrapper = Main.getSocketWrapper();
        MemberRequest memberRequest = new MemberRequest(bookId, currentMember.getUserId(), 1);
//            server.librarianService.requestBorrowedBook(currentMember, bookId);
        socketWrapper.write(memberRequest);
        borrowMessageLabel.setText("Borrow request sent for Book ID: " + bookId);
        borrowBookIdField.clear();
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

            SocketWrapper socketWrapper = Main.getSocketWrapper();
            MemberRequest memberRequest = new MemberRequest(bookId, currentMember.getUserId(), ratingStr);
            socketWrapper.write(memberRequest);


//            server.librarianService.returnBorrowedBook(currentMember, bookId, rating);
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

    @FXML void handleRefresh(ActionEvent event) {
        System.out.println("Manual refresh triggered for member");
        loadData();
    }

    @FXML void handleLogout(ActionEvent event) throws IOException {
        // Send logout request to server to clean up client map
        if (SceneManager.getCurrentUser() != null) {
            SocketWrapper socketWrapper = Main.getSocketWrapper();
            if (socketWrapper != null) {
                LogoutRequest logoutRequest = new LogoutRequest(SceneManager.getCurrentUser().getUserId());
                socketWrapper.write(logoutRequest);
            }
        }

        // Clear the controller reference
        Main.setMemberController(null);

        // Clear the static data package
        Main.setMemberPackage(null);

        // Clear the current user session
        SceneManager.setCurrentUser(null);

        // Go back to the login screen
        SceneManager.switchScene("/Main/login-view.fxml");
    }
}