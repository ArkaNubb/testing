package Main;

import common.Book;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import service.AuthorService;
import service.server;
import common.Author;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AuthorController {

    private Author currentAuthor;
    private AuthorService authorService; // Service instance for the current author

    @FXML private Label welcomeLabel;
    @FXML private Label publishMessageLabel;
    @FXML private Label removeMessageLabel;

    @FXML private TableView<Book> myBooksTable;
    @FXML private TableColumn<Book, String> bookTitleCol;
    @FXML private TableColumn<Book, String> bookIdCol;
    @FXML private TableColumn<Book, String> bookDateCol;
    @FXML private TableColumn<Book, Number> bookRatingCol;

    @FXML private TextField titleField;
    @FXML private TextField dateField;
    @FXML private TextField genresField;
    @FXML private TextField copiesField;

    @FXML
    public void initialize() {
        currentAuthor = (Author) SceneManager.getCurrentUser();
        if (currentAuthor != null) {
            welcomeLabel.setText("Welcome, " + currentAuthor.getName());
            authorService = new AuthorService(currentAuthor);
        }
        setupTable();
        loadData();
    }

    private void setupTable() {
        bookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        bookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
        bookDateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPublishedDate()));
        bookRatingCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getRating()));
    }

    private void loadData() {
        if (currentAuthor != null) {
            // This requires the getPublishedBooks() method which you already have in Author.java
            myBooksTable.setItems(FXCollections.observableArrayList(currentAuthor.getPublishedBooks()));
            myBooksTable.refresh();
        }
    }

    @FXML
    void handlePublishRequest(ActionEvent event) {
        String title = titleField.getText();
        String date = dateField.getText();
        String genresStr = genresField.getText();
        String copiesStr = copiesField.getText();

        if (title.isEmpty() || date.isEmpty() || genresStr.isEmpty() || copiesStr.isEmpty()) {
            publishMessageLabel.setText("All fields are required.");
            return;
        }

        try {
            int copies = Integer.parseInt(copiesStr);
            List<String> genres = Arrays.asList(genresStr.split(","));
            String bookId = server.authorService.generateBookId();

            Book newBook = new Book(title, bookId, date, currentAuthor.getName(), genres, new ArrayList<>(), copies, copies);

            server.librarianService.requestPublishBook(currentAuthor, newBook);
            publishMessageLabel.setText("Publish request sent for '" + title + "'. Awaiting librarian approval.");

            titleField.clear();
            dateField.clear();
            genresField.clear();
            copiesField.clear();

        } catch (NumberFormatException e) {
            publishMessageLabel.setText("Invalid number for copies.");
        } catch (IOException e) {
            publishMessageLabel.setText("Error sending publish request.");
            e.printStackTrace();
        }
    }

    @FXML
    void handleRemoveBook(ActionEvent event) {
        Book selectedBook = myBooksTable.getSelectionModel().getSelectedItem();
        removeMessageLabel.setText("");

        if (selectedBook == null) {
            removeMessageLabel.setText("Please select a book to remove.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Removal");
        confirmationAlert.setHeaderText("Remove '" + selectedBook.getName() + "'?");
        confirmationAlert.setContentText("Are you sure you want to permanently remove this book from the library system?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                authorService.RemoveBook(selectedBook);
                removeMessageLabel.setText("Book '" + selectedBook.getName() + "' has been removed.");
                loadData();
            } catch (IOException e) {
                removeMessageLabel.setText("Error removing book. Please check file permissions.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleLogout(ActionEvent event) throws IOException {
        SceneManager.setCurrentUser(null);
        SceneManager.switchScene("/Main/login-view.fxml");
    }
}