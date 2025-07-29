package Main;

import common.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import service.server;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MemberController implements Initializable {

    private Member currentMember;

    // Header and Navigation
    @FXML private Label welcomeLabel;
    @FXML private Button backButton;

    // Message Labels
    @FXML private Label borrowMessageLabel;
    @FXML private Label returnMessageLabel;
    @FXML private Label searchMessageLabel;

    // Navigation Views
    @FXML private VBox dashboardView;
    @FXML private VBox availableBooksView;
    @FXML private VBox myBooksView;
    @FXML private VBox borrowBooksView;

    // Dashboard Components
    @FXML private Label totalBooksLabel;
    @FXML private Label borrowedBooksLabel;

    // Available Books View Components
    @FXML private TableView<Book> allBooksTable;
    @FXML private TableColumn<Book, String> allBookTitleCol;
    @FXML private TableColumn<Book, String> allBookAuthorCol;
    @FXML private TableColumn<Book, String> allBookIdCol;
    @FXML private TableColumn<Book, String> allBookGenreCol;
    @FXML private TableColumn<Book, Number> allBookAvailableCol;
    @FXML private TableColumn<Book, Number> allBookRatingCol;

    // Search functionality for Available Books
    @FXML private TextField searchBookNameField;
    @FXML private TextField searchGenreField;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;

    // My Books View Components
    @FXML private TableView<Book> myBooksTable;
    @FXML private TableColumn<Book, String> myBookTitleCol;
    @FXML private TableColumn<Book, String> myBookAuthorCol;
    @FXML private TableColumn<Book, String> myBookIdCol;
    @FXML private TableColumn<Book, String> myBookGenreCol;
    @FXML private TableColumn<Book, Number> myBookRatingCol;
    @FXML private TextField returnBookIdField;
    @FXML private TextField ratingField;

    // Borrow Books View Components
    @FXML private TextField borrowSearchNameField;
    @FXML private TextField borrowSearchGenreField;
    @FXML private TextField borrowBookIdField;
    @FXML private TableView<Book> borrowBooksTable;
    @FXML private TableColumn<Book, String> borrowBookIdCol;
    @FXML private TableColumn<Book, String> borrowBookTitleCol;
    @FXML private TableColumn<Book, String> borrowBookAuthorCol;
    @FXML private TableColumn<Book, String> borrowBookGenreCol;
    @FXML private TableColumn<Book, Number> borrowBookAvailableCol;
    @FXML private TableColumn<Book, Number> borrowBookRatingCol;

    // Data Collections
    private final ObservableList<Book> allBooksData = FXCollections.observableArrayList();
    private final ObservableList<Book> myBooksData = FXCollections.observableArrayList();
    private final ObservableList<Book> filteredBooksData = FXCollections.observableArrayList();
    private final ObservableList<Book> borrowBooksData = FXCollections.observableArrayList();

    // Store all books for filtering
    private List<Book> allBooksList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentMember = (Member) SceneManager.getCurrentUser();
        if (currentMember != null) {
            welcomeLabel.setText("Welcome, " + currentMember.getName());
        }

        setupTables();
        setupBorrowBooksTable();

        // Set the controller instance in Main for real-time updates
        Main.setMemberController(this);

        // Load initial data and update dashboard stats
        loadData();
        updateDashboardStats();

        // Start with dashboard view
        showView("dashboard");
    }

    private void setupTables() {
        // Setup cell value factories for all books table
        allBookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        allBookAuthorCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthorName()));
        allBookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
        allBookGenreCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getGenre() != null ? String.join(", ", cellData.getValue().getGenre()) : "N/A"));
        allBookAvailableCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAvailable_copies()));
        allBookRatingCol.setCellValueFactory(cellData -> {
            double rating = cellData.getValue().getRating();
            return new SimpleDoubleProperty(rating == 0.0 ? 0.0 : Math.round(rating * 10.0) / 10.0);
        });

        // Setup cell value factories for my books table
        myBookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        myBookAuthorCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthorName()));
        myBookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
        myBookGenreCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getGenre() != null ? String.join(", ", cellData.getValue().getGenre()) : "N/A"));
        myBookRatingCol.setCellValueFactory(cellData -> {
            double rating = cellData.getValue().getRating();
            return new SimpleDoubleProperty(rating == 0.0 ? 0.0 : Math.round(rating * 10.0) / 10.0);
        });

        // Format rating columns to show one decimal place
        allBookRatingCol.setCellFactory(column -> {
            return new TableCell<Book, Number>() {
                @Override
                protected void updateItem(Number item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        double rating = item.doubleValue();
                        setText(rating == 0.0 ? "N/A" : String.format("%.1f", rating));
                    }
                }
            };
        });

        myBookRatingCol.setCellFactory(column -> {
            return new TableCell<Book, Number>() {
                @Override
                protected void updateItem(Number item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        double rating = item.doubleValue();
                        setText(rating == 0.0 ? "N/A" : String.format("%.1f", rating));
                    }
                }
            };
        });

        // Set the ObservableList to tables
        allBooksTable.setItems(allBooksData);
        myBooksTable.setItems(myBooksData);
    }

    private void setupBorrowBooksTable() {
        // Setup cell value factories for borrow books table
        borrowBookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        borrowBookAuthorCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthorName()));
        borrowBookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
        borrowBookGenreCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getGenre() != null ? String.join(", ", cellData.getValue().getGenre()) : "N/A"));
        borrowBookAvailableCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAvailable_copies()));
        borrowBookRatingCol.setCellValueFactory(cellData -> {
            double rating = cellData.getValue().getRating();
            return new SimpleDoubleProperty(rating == 0.0 ? 0.0 : Math.round(rating * 10.0) / 10.0);
        });

        // Format rating column for borrow books table
        borrowBookRatingCol.setCellFactory(column -> {
            return new TableCell<Book, Number>() {
                @Override
                protected void updateItem(Number item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        double rating = item.doubleValue();
                        setText(rating == 0.0 ? "N/A" : String.format("%.1f", rating));
                    }
                }
            };
        });

        // Set the ObservableList to borrow books table
        borrowBooksTable.setItems(borrowBooksData);
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
            filteredBooksData.clear();
            borrowBooksData.clear();

            // Update all books data
            if (memberPackage.getAllBooks() != null) {
                allBooksList = memberPackage.getAllBooks();
                allBooksData.addAll(allBooksList);
                filteredBooksData.addAll(allBooksList);
                borrowBooksData.addAll(allBooksList);
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
            borrowBooksTable.refresh();

            // Update dashboard stats if on dashboard
            if (dashboardView != null && dashboardView.isVisible()) {
                updateDashboardStats();
            }

            System.out.println("=== Member UI Update Complete ===");
        });
    }

    // Navigation Methods
    @FXML
    void handleShowAvailableBooks(MouseEvent event) {
        showView("available");
        loadData();
    }

    @FXML
    void handleShowMyBooks(MouseEvent event) {
        showView("mybooks");
        loadData();
    }

    @FXML
    void handleShowBorrowBooks(MouseEvent event) {
        showView("borrow");
        loadData();
    }

    @FXML
    void handleBackToDashboard(ActionEvent event) {
        showView("dashboard");
        updateDashboardStats();
        clearAllMessages();
    }

    private void showView(String viewName) {
        // Hide all views
        if (dashboardView != null) {
            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
        }
        if (availableBooksView != null) {
            availableBooksView.setVisible(false);
            availableBooksView.setManaged(false);
        }
        if (myBooksView != null) {
            myBooksView.setVisible(false);
            myBooksView.setManaged(false);
        }
        if (borrowBooksView != null) {
            borrowBooksView.setVisible(false);
            borrowBooksView.setManaged(false);
        }

        // Show selected view
        switch (viewName.toLowerCase()) {
            case "dashboard":
                if (dashboardView != null) {
                    dashboardView.setVisible(true);
                    dashboardView.setManaged(true);
                }
                hideBackButton();
                break;
            case "available":
                if (availableBooksView != null) {
                    availableBooksView.setVisible(true);
                    availableBooksView.setManaged(true);
                }
                showBackButton();
                break;
            case "mybooks":
                if (myBooksView != null) {
                    myBooksView.setVisible(true);
                    myBooksView.setManaged(true);
                }
                showBackButton();
                break;
            case "borrow":
                if (borrowBooksView != null) {
                    borrowBooksView.setVisible(true);
                    borrowBooksView.setManaged(true);
                }
                showBackButton();
                break;
        }
    }

    private void showBackButton() {
        if (backButton != null) {
            backButton.setVisible(true);
            backButton.setManaged(true);
        }
    }

    private void hideBackButton() {
        if (backButton != null) {
            backButton.setVisible(false);
            backButton.setManaged(false);
        }
    }

    private void updateDashboardStats() {
        Platform.runLater(() -> {
            MemberPackage memberPackage = Main.getMemberPackage();
            if (memberPackage != null) {
                // Update total books count
                if (memberPackage.getAllBooks() != null && totalBooksLabel != null) {
                    totalBooksLabel.setText(String.valueOf(memberPackage.getAllBooks().size()));
                } else if (totalBooksLabel != null) {
                    totalBooksLabel.setText("0");
                }

                // Update borrowed books count
                Member updatedMember = memberPackage.getMember();
                if (updatedMember != null && updatedMember.getBorrowedBooks() != null && borrowedBooksLabel != null) {
                    borrowedBooksLabel.setText(String.valueOf(updatedMember.getBorrowedBooks().size()));
                } else if (borrowedBooksLabel != null) {
                    borrowedBooksLabel.setText("0");
                }
            }
        });
    }

    // Search Methods
    @FXML
    void handleSearchBooks(ActionEvent event) {
        String searchName = searchBookNameField.getText().trim().toLowerCase();
        String searchGenre = searchGenreField.getText().trim().toLowerCase();

        if (searchName.isEmpty() && searchGenre.isEmpty()) {
            searchMessageLabel.setText("Please enter book name or genre to search.");
            return;
        }

        if (allBooksList == null || allBooksList.isEmpty()) {
            searchMessageLabel.setText("No books available to search.");
            return;
        }

        List<Book> searchResults = allBooksList.stream()
                .filter(book -> {
                    boolean nameMatch = searchName.isEmpty() ||
                            book.getName().toLowerCase().contains(searchName);

                    boolean genreMatch = searchGenre.isEmpty() ||
                            (book.getGenre() != null &&
                                    book.getGenre().stream().anyMatch(genre ->
                                            genre.toLowerCase().contains(searchGenre)));

                    return nameMatch && genreMatch;
                })
                .collect(Collectors.toList());

        Platform.runLater(() -> {
            allBooksData.clear();
            allBooksData.addAll(searchResults);
            allBooksTable.refresh();

            searchMessageLabel.setText("Found " + searchResults.size() + " book(s) matching your search.");
        });
    }

    @FXML
    void handleClearSearch(ActionEvent event) {
        if (searchBookNameField != null) searchBookNameField.clear();
        if (searchGenreField != null) searchGenreField.clear();
        if (searchMessageLabel != null) searchMessageLabel.setText("");

        // Reset to show all books
        if (allBooksList != null) {
            Platform.runLater(() -> {
                allBooksData.clear();
                allBooksData.addAll(allBooksList);
                allBooksTable.refresh();
            });
        }
    }

    @FXML
    void handleBorrowSearch(ActionEvent event) {
        String searchName = borrowSearchNameField.getText().trim().toLowerCase();
        String searchGenre = borrowSearchGenreField.getText().trim().toLowerCase();

        if (searchName.isEmpty() && searchGenre.isEmpty()) {
            borrowMessageLabel.setText("Please enter book name or genre to search.");
            return;
        }

        if (allBooksList == null || allBooksList.isEmpty()) {
            borrowMessageLabel.setText("No books available to search.");
            return;
        }

        List<Book> searchResults = allBooksList.stream()
                .filter(book -> {
                    boolean nameMatch = searchName.isEmpty() ||
                            book.getName().toLowerCase().contains(searchName);

                    boolean genreMatch = searchGenre.isEmpty() ||
                            (book.getGenre() != null &&
                                    book.getGenre().stream().anyMatch(genre ->
                                            genre.toLowerCase().contains(searchGenre)));

                    return nameMatch && genreMatch;
                })
                .collect(Collectors.toList());

        Platform.runLater(() -> {
            borrowBooksData.clear();
            borrowBooksData.addAll(searchResults);
            borrowBooksTable.refresh();

            borrowMessageLabel.setText("Found " + searchResults.size() + " book(s) matching your search.");
        });
    }

    @FXML
    void handleBorrowClearSearch(ActionEvent event) {
        if (borrowSearchNameField != null) borrowSearchNameField.clear();
        if (borrowSearchGenreField != null) borrowSearchGenreField.clear();
        if (borrowMessageLabel != null) borrowMessageLabel.setText("");

        // Reset to show all books
        if (allBooksList != null) {
            Platform.runLater(() -> {
                borrowBooksData.clear();
                borrowBooksData.addAll(allBooksList);
                borrowBooksTable.refresh();
            });
        }
    }

    // Book Operations
    @FXML
    void handleRequestBorrow(ActionEvent event) throws IOException {
        String bookId = borrowBookIdField.getText().trim();
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
        socketWrapper.write(memberRequest);
        borrowMessageLabel.setText("Borrow request sent for Book ID: " + bookId);
        borrowBookIdField.clear();
    }

    @FXML
    void handleRequestReturn(ActionEvent event) {
        String bookId = returnBookIdField.getText().trim();
        String ratingStr = ratingField.getText().trim();

        if (bookId.isEmpty()) {
            returnMessageLabel.setText("Please enter a Book ID.");
            return;
        }

        if (ratingStr.isEmpty()) {
            ratingStr = "0.0";
        }

        try {
            double rating = Double.parseDouble(ratingStr);
            if (rating < 0 || rating > 10) {
                returnMessageLabel.setText("Rating must be between 0 and 10.");
                return;
            }

            SocketWrapper socketWrapper = Main.getSocketWrapper();
            MemberRequest memberRequest = new MemberRequest(bookId, currentMember.getUserId(), ratingStr);
            socketWrapper.write(memberRequest);

            returnMessageLabel.setText("Return request sent for Book ID: " + bookId + " with rating: " + rating);
            returnBookIdField.clear();
            ratingField.clear();
        } catch (NumberFormatException e) {
            returnMessageLabel.setText("Invalid rating. Please enter a number between 0 and 10.");
        } catch (IOException e) {
            returnMessageLabel.setText("Error sending request.");
            e.printStackTrace();
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        System.out.println("Manual refresh triggered for member");
        loadData();
        clearAllMessages();

        // Update dashboard stats if on dashboard
        if (dashboardView != null && dashboardView.isVisible()) {
            updateDashboardStats();
        }
    }

    private void clearAllMessages() {
        if (borrowMessageLabel != null) borrowMessageLabel.setText("");
        if (returnMessageLabel != null) returnMessageLabel.setText("");
        if (searchMessageLabel != null) searchMessageLabel.setText("");
    }

    @FXML
    void handleLogout(ActionEvent event) throws IOException {
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