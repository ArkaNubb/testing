package Main;

import common.Book;
import common.Author;
import common.AuthorRequest;
import common.LogoutRequest;
import common.SocketWrapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox; // Import VBox for view management

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AuthorController implements Initializable {

    private Author currentAuthor;
    private final ObservableList<Book> myBooksData = FXCollections.observableArrayList();

    // FXML fields from the original file
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

    // New FXML fields for the different views (panes)
    @FXML private VBox initialView;
    @FXML private VBox publishedBooksView;
    @FXML private VBox requestBookView;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentAuthor = (Author) SceneManager.getCurrentUser();
        if (currentAuthor != null) {
            welcomeLabel.setText("Welcome, " + currentAuthor.getName());
        }
        setupTable();

        // Ensure the initial menu is showing when the scene loads
        showInitialView();

        Main.setAuthorController(this);
        loadData();
    }

    
    private void setViewVisibility(VBox visibleView) {
        // Hide all views
        initialView.setVisible(false);
        initialView.setManaged(false);
        publishedBooksView.setVisible(false);
        publishedBooksView.setManaged(false);
        requestBookView.setVisible(false);
        requestBookView.setManaged(false);

        // Show and manage the selected view
        if (visibleView != null) {
            visibleView.setVisible(true);
            visibleView.setManaged(true);
        }
    }

    
    private void showInitialView() {
        setViewVisibility(initialView);
    }

    
    @FXML
    void showMyBooks(ActionEvent event) {
        setViewVisibility(publishedBooksView);
    }

    
    @FXML
    void showPublishRequest(ActionEvent event) {
        setViewVisibility(requestBookView);
    }

    
    @FXML
    void goBack(ActionEvent event) {
        
        publishMessageLabel.setText("");
        removeMessageLabel.setText("");
        showInitialView();
    }

    

    private void setupTable() {
        bookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        bookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
        bookDateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPublishedDate()));
        bookRatingCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getRating()));
        myBooksTable.setItems(myBooksData);
    }

    public void loadData() {
        Platform.runLater(() -> {
            System.out.println("=== Refreshing Author UI ===");
            myBooksData.clear();
            if (Main.getAuthorPackage() != null) {
                Author updatedAuthor = Main.getAuthorPackage().getAuthor();
                if (updatedAuthor != null && updatedAuthor.getPublishedBooks() != null) {
                    myBooksData.addAll(updatedAuthor.getPublishedBooks());
                    currentAuthor = updatedAuthor;
                }
            } else if (currentAuthor != null && currentAuthor.getPublishedBooks() != null) {
                myBooksData.addAll(currentAuthor.getPublishedBooks());
            }
            myBooksTable.refresh();
            System.out.println("=== Author UI Update Complete ===");
        });
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
            if (copies <= 0) {
                publishMessageLabel.setText("Number of copies must be positive.");
                return;
            }

            List<String> genres = new ArrayList<>(Arrays.asList(genresStr.split(",")));
            genres.replaceAll(String::trim);

            SocketWrapper socketWrapper = Main.getSocketWrapper();
            if (socketWrapper != null) {
                AuthorRequest publishRequest = new AuthorRequest(
                        currentAuthor.getUserId(),
                        title,
                        date,
                        genres,
                        copies
                );

                socketWrapper.write(publishRequest);
                publishMessageLabel.setText("Publish request sent. Awaiting approval.");

                titleField.clear();
                dateField.clear();
                genresField.clear();
                copiesField.clear();
            } else {
                publishMessageLabel.setText("Connection to server not established.");
            }

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
        confirmationAlert.setContentText("Are you sure you want to permanently remove this book?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                SocketWrapper socketWrapper = Main.getSocketWrapper();
                if (socketWrapper != null) {
                    AuthorRequest removeRequest = new AuthorRequest(
                            currentAuthor.getUserId(),
                            selectedBook.getBookId(),
                            AuthorRequest.REMOVE_BOOK
                    );

                    socketWrapper.write(removeRequest);
                    removeMessageLabel.setText("Remove request sent for '" + selectedBook.getName() + "'.");
                } else {
                    removeMessageLabel.setText("Connection to server not established.");
                }

            } catch (IOException e) {
                removeMessageLabel.setText("Error sending remove request to server.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleLogout(ActionEvent event) throws IOException {
        if (SceneManager.getCurrentUser() != null) {
            SocketWrapper socketWrapper = Main.getSocketWrapper();
            if (socketWrapper != null) {
                LogoutRequest logoutRequest = new LogoutRequest(SceneManager.getCurrentUser().getUserId());
                socketWrapper.write(logoutRequest);
            }
        }
        Main.setAuthorController(null);
        Main.setAuthorPackage(null);
        SceneManager.setCurrentUser(null);
        SceneManager.switchScene("/Main/login-view.fxml");
    }
}
