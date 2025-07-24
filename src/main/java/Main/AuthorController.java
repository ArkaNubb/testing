package Main;

import book.Book;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import user.Author;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuthorController {

    private Author currentAuthor;
    @FXML private Button logoutButton;

    @FXML private Label welcomeLabel;
    @FXML private Label publishMessageLabel;

    // My Books Table
    @FXML private TableView<Book> myBooksTable;
    @FXML private TableColumn<Book, String> bookTitleCol;
    @FXML private TableColumn<Book, String> bookIdCol;
    @FXML private TableColumn<Book, String> bookDateCol;
    @FXML private TableColumn<Book, Number> bookRatingCol;

    // Publish Form
    @FXML private TextField titleField;
    @FXML private TextField dateField;
    @FXML private TextField genresField;
    @FXML private TextField copiesField;

    @FXML
    public void initialize() {
        currentAuthor = (Author) SceneManager.getCurrentUser();
        if (currentAuthor != null) {
            welcomeLabel.setText("Welcome, " + currentAuthor.getName());
        }
        setupTable();
        loadData();
    }

    private void setupTable() {
        // Use callbacks to get data from standard getters
        bookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        bookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
        bookDateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPublishedDate()));
        bookRatingCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getRating()));
    }

    private void loadData() {
        if (currentAuthor != null) {
            myBooksTable.setItems(FXCollections.observableArrayList(currentAuthor.getPublishedBooks()));
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
            String bookId = Main.authorService.generateBookId();

            Book newBook = new Book(title, bookId, date, currentAuthor.getName(), genres, new ArrayList<>(), copies, copies);

            Main.librarianService.requestPublishBook(currentAuthor, newBook);
            publishMessageLabel.setText("Publish request sent for '" + title + "'. Awaiting librarian approval.");

            // Clear form fields
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
    void handleLogout(ActionEvent event) throws IOException {
        // Clear the currently logged-in user's data
        SceneManager.setCurrentUser(null);

        // Switch the scene back to the login view
        SceneManager.switchScene("/Main/login-view.fxml");
    }
}
