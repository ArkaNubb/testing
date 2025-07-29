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
import javafx.scene.input.KeyEvent;
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

    @FXML private Label welcomeLabel;
    @FXML private Label totalBooksLabel;
    @FXML private Label myBooksCountLabel;
    @FXML private Label borrowMessageLabel;
    @FXML private Label returnMessageLabel;
    @FXML private Label searchMessageLabel;
    @FXML private Label browseCountLabel;

    // Navigation Buttons
    @FXML private Button browseLibraryButton;
    @FXML private Button myBooksButton;
    @FXML private Button borrowBookButton;

    // Main Content Panels
    @FXML private VBox browseLibraryPanel;
    @FXML private VBox myBooksPanel;
    @FXML private VBox borrowBookPanel;

    // Browse Library Components
    @FXML private TableView<Book> browseTable;
    @FXML private TableColumn<Book, String> browseTitleCol;
    @FXML private TableColumn<Book, String> browseAuthorCol;
    @FXML private TableColumn<Book, String> browseIdCol;
    @FXML private TableColumn<Book, String> browseGenreCol;
    @FXML private TableColumn<Book, Number> browseAvailableCol;
    @FXML private TableColumn<Book, Number> browseRatingCol;
    @FXML private TextField browseSearchField;

    // My Books Components
    @FXML private TableView<Book> myBooksTable;
    @FXML private TableColumn<Book, String> myBookTitleCol;
    @FXML private TableColumn<Book, String> myBookAuthorCol;
    @FXML private TableColumn<Book, String> myBookIdCol;
    @FXML private TableColumn<Book, String> myBookGenreCol;
    @FXML private TableColumn<Book, Number> myBookRatingCol;
    @FXML private TextField returnBookIdField;
    @FXML private TextField ratingField;

    // Borrow Book Components
    @FXML private TableView<Book> borrowTable;
    @FXML private TableColumn<Book, String> borrowTitleCol;
    @FXML private TableColumn<Book, String> borrowAuthorCol;
    @FXML private TableColumn<Book, String> borrowIdCol;
    @FXML private TableColumn<Book, String> borrowGenreCol;
    @FXML private TableColumn<Book, Number> borrowAvailableCol;
    @FXML private TableColumn<Book, Number> borrowRatingCol;
    @FXML private TextField borrowSearchField;
    @FXML private TextField borrowBookIdField;

    private final ObservableList<Book> browseData = FXCollections.observableArrayList();
    private final ObservableList<Book> myBooksData = FXCollections.observableArrayList();
    private final ObservableList<Book> borrowData = FXCollections.observableArrayList();

    // Store all books for filtering
    private List<Book> allBooksList;
    private String currentActivePanel = "browse"; // browse, myBooks, borrow

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentMember = (Member) SceneManager.getCurrentUser();
        if (currentMember != null) {
            welcomeLabel.setText("Welcome, " + currentMember.getName());
        }
        setupTables();
        setupTableInteractions();
        showBrowseLibrary(); // Show browse library by default

        // Set the controller instance in Main for real-time updates
        Main.setMemberController(this);

        // Load initial data
        loadData();

        // Update counters
        updateBookCounters();
    }

    private void setupTables() {
        // Setup Browse Library Table
        setupTableColumns(browseTitleCol, browseAuthorCol, browseIdCol, browseGenreCol, browseAvailableCol, browseRatingCol);
        browseTable.setItems(browseData);

        // Setup My Books Table
        setupTableColumns(myBookTitleCol, myBookAuthorCol, myBookIdCol, myBookGenreCol, null, myBookRatingCol);
        myBooksTable.setItems(myBooksData);

        // Setup Borrow Book Table
        setupTableColumns(borrowTitleCol, borrowAuthorCol, borrowIdCol, borrowGenreCol, borrowAvailableCol, borrowRatingCol);
        borrowTable.setItems(borrowData);

        // Enhanced table styling
        applyTableStyling(browseTable);
        applyTableStyling(myBooksTable);
        applyTableStyling(borrowTable);
    }

    private void applyTableStyling(TableView<Book> table) {
        table.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: rgba(99, 102, 241, 0.05); -fx-cursor: hand;");
                }
            });
            row.setOnMouseExited(e -> {
                if (!row.isEmpty() && !row.isSelected()) {
                    row.setStyle("");
                }
            });
            return row;
        });
    }

    private void setupTableInteractions() {
        // Auto-fill book ID when clicking on table rows
        browseTable.setOnMouseClicked(this::handleBrowseTableClick);
        myBooksTable.setOnMouseClicked(this::handleMyBooksTableClick);
        borrowTable.setOnMouseClicked(this::handleBorrowTableClick);
    }

    private void handleBrowseTableClick(MouseEvent event) {
        if (event.getClickCount() == 2) { // Double click
            Book selected = browseTable.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getAvailable_copies() > 0) {
                // Switch to borrow panel and pre-fill the book ID
                showBorrowBook();
                borrowBookIdField.setText(selected.getBookId());
            }
        }
    }

    private void handleMyBooksTableClick(MouseEvent event) {
        Book selected = myBooksTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            returnBookIdField.setText(selected.getBookId());
        }
    }

    private void handleBorrowTableClick(MouseEvent event) {
        Book selected = borrowTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            borrowBookIdField.setText(selected.getBookId());
        }
    }

    private void setupTableColumns(TableColumn<Book, String> titleCol, TableColumn<Book, String> authorCol,
                                   TableColumn<Book, String> idCol, TableColumn<Book, String> genreCol,
                                   TableColumn<Book, Number> availableCol, TableColumn<Book, Number> ratingCol) {

        titleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        authorCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthorName()));
        idCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
        genreCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getGenre() != null ? String.join(", ", cellData.getValue().getGenre()) : "N/A"));

        if (availableCol != null) {
            availableCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAvailable_copies()));

            // Style available column with colored indicators
            availableCol.setCellFactory(column -> {
                return new TableCell<Book, Number>() {
                    @Override
                    protected void updateItem(Number item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            int available = item.intValue();
                            setText(String.valueOf(available));

                            if (available == 0) {
                                setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                            } else if (available <= 2) {
                                setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                            } else {
                                setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                            }
                        }
                    }
                };
            });
        }

        ratingCol.setCellValueFactory(cellData -> {
            double rating = cellData.getValue().getRating();
            return new SimpleDoubleProperty(rating == 0.0 ? 0.0 : Math.round(rating * 10.0) / 10.0);
        });

        // Enhanced rating column with star styling
        ratingCol.setCellFactory(column -> {
            return new TableCell<Book, Number>() {
                @Override
                protected void updateItem(Number item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        double rating = item.doubleValue();
                        if (rating == 0.0) {
                            setText("N/A");
                            setStyle("-fx-text-fill: #9ca3af;");
                        } else {
                            setText(String.format("%.1f ⭐", rating));
                            if (rating >= 8.0) {
                                setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                            } else if (rating >= 6.0) {
                                setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                            } else {
                                setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                            }
                        }
                    }
                }
            };
        });
    }

    public void loadData() {
        Platform.runLater(() -> {
            System.out.println("Refreshing Member UI");

            MemberPackage memberPackage = Main.getMemberPackage();
            if (memberPackage == null) {
                System.out.println("Member package is null. Cannot load data");
                return;
            }

            // Clear previous data
            browseData.clear();
            myBooksData.clear();
            borrowData.clear();

            // Update all books data
            if (memberPackage.getAllBooks() != null) {
                allBooksList = memberPackage.getAllBooks();
                browseData.addAll(allBooksList);

                // For borrow panel, only show available books
                List<Book> availableBooks = allBooksList.stream()
                        .filter(book -> book.getAvailable_copies() > 0)
                        .collect(Collectors.toList());
                borrowData.addAll(availableBooks);

                System.out.println("Loaded " + browseData.size() + " books in browse table");
                System.out.println("Loaded " + availableBooks.size() + " available books in borrow table");
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
            browseTable.refresh();
            myBooksTable.refresh();
            borrowTable.refresh();

            // Update counters and UI
            updateBookCounters();
            updateStatusMessages();

            System.out.println("=== Member UI Update Complete ===");
        });
    }

    private void updateBookCounters() {
        Platform.runLater(() -> {
            // Update total books count
            int totalBooks = browseData.size();
            totalBooksLabel.setText(String.valueOf(totalBooks));

            // Update my books count
            int myBooksCount = myBooksData.size();
            myBooksCountLabel.setText(myBooksCount + "/5");

            // Update browse count label
            if (browseCountLabel != null) {
                browseCountLabel.setText(totalBooks + " books");
            }

            // Update button text to include counts (optional)
            browseLibraryButton.setText("📚 Browse Library (" + totalBooks + ")");
            myBooksButton.setText("📋 My Books (" + myBooksCount + ")");

            // Update available books count for borrow panel
            int availableBooks = (int) borrowData.stream().count();
            borrowBookButton.setText("📥 Borrow Book (" + availableBooks + ")");
        });
    }

    private void updateStatusMessages() {
        Platform.runLater(() -> {
            if (currentMember != null) {
                int borrowedCount = myBooksData.size();
                if (borrowedCount >= 5) {
                    if (borrowMessageLabel != null) {
                        borrowMessageLabel.setText("⚠️ You have reached the maximum borrow limit (5 books)");
                        borrowMessageLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 13px; -fx-font-weight: 600;");
                    }
                } else {
                    if (borrowMessageLabel != null) {
                        borrowMessageLabel.setText("✅ You can borrow " + (5 - borrowedCount) + " more book(s)");
                        borrowMessageLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 13px; -fx-font-weight: 500;");
                    }
                }
            }
        });
    }

    // Navigation Methods with enhanced animations
    @FXML
    void handleBrowseLibrary(ActionEvent event) {
        showBrowseLibrary();
    }

    @FXML
    void handleMyBooks(ActionEvent event) {
        showMyBooks();
    }

    @FXML
    void handleBorrowBook(ActionEvent event) {
        showBorrowBook();
    }

    private void showBrowseLibrary() {
        currentActivePanel = "browse";
        updateButtonStyles();
        switchPanel(browseLibraryPanel);
        clearMessages();
    }

    private void showMyBooks() {
        currentActivePanel = "myBooks";
        updateButtonStyles();
        switchPanel(myBooksPanel);
        clearMessages();
        loadData(); // Refresh my books data
    }

    private void showBorrowBook() {
        currentActivePanel = "borrow";
        updateButtonStyles();
        switchPanel(borrowBookPanel);
        clearMessages();
        updateStatusMessages();
    }

    private void switchPanel(VBox targetPanel) {
        // Hide all panels
        browseLibraryPanel.setVisible(false);
        browseLibraryPanel.setManaged(false);
        myBooksPanel.setVisible(false);
        myBooksPanel.setManaged(false);
        borrowBookPanel.setVisible(false);
        borrowBookPanel.setManaged(false);

        // Show target panel
        targetPanel.setVisible(true);
        targetPanel.setManaged(true);
    }

    private void updateButtonStyles() {
        String activeStyle = "-fx-background-color: linear-gradient(135deg, #3b82f6, #1d4ed8); -fx-text-fill: white; -fx-border-radius: 15; -fx-background-radius: 15; -fx-padding: 15 30 15 30; -fx-font-size: 14px; -fx-font-weight: 600; -fx-cursor: hand; -fx-min-width: 180; -fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 12, 0, 0, 0);";
        String inactiveStyle = "-fx-background-color: rgba(99, 102, 241, 0.1); -fx-text-fill: #6366f1; -fx-border-color: rgba(99, 102, 241, 0.3); -fx-border-width: 2; -fx-border-radius: 15; -fx-background-radius: 15; -fx-padding: 15 30 15 30; -fx-font-size: 14px; -fx-font-weight: 600; -fx-cursor: hand; -fx-min-width: 180;";

        browseLibraryButton.setStyle(currentActivePanel.equals("browse") ? activeStyle : inactiveStyle);
        myBooksButton.setStyle(currentActivePanel.equals("myBooks") ? activeStyle : inactiveStyle);
        borrowBookButton.setStyle(currentActivePanel.equals("borrow") ? activeStyle : inactiveStyle);
    }

    private void clearMessages() {
        if (borrowMessageLabel != null) borrowMessageLabel.setText("");
        if (returnMessageLabel != null) returnMessageLabel.setText("");
        if (searchMessageLabel != null) searchMessageLabel.setText("");
    }

    // Enhanced Search Methods with real-time filtering
    @FXML
    void handleBrowseSearch(KeyEvent event) {
        String searchTerm = browseSearchField.getText().trim().toLowerCase();
        performBrowseSearch(searchTerm);
    }

    @FXML
    void handleBorrowSearch(KeyEvent event) {
        String searchTerm = borrowSearchField.getText().trim().toLowerCase();
        performBorrowSearch(searchTerm);
    }

    private void performBrowseSearch(String searchTerm) {
        if (allBooksList == null) return;

        Platform.runLater(() -> {
            if (searchTerm.isEmpty()) {
                browseData.clear();
                browseData.addAll(allBooksList);
            } else {
                List<Book> searchResults = allBooksList.stream()
                        .filter(book -> {
                            boolean nameMatch = book.getName().toLowerCase().contains(searchTerm);
                            boolean authorMatch = book.getAuthorName().toLowerCase().contains(searchTerm);
                            boolean genreMatch = book.getGenre() != null &&
                                    book.getGenre().stream().anyMatch(genre ->
                                            genre.toLowerCase().contains(searchTerm));
                            return nameMatch || authorMatch || genreMatch;
                        })
                        .collect(Collectors.toList());

                browseData.clear();
                browseData.addAll(searchResults);
            }

            browseTable.refresh();
            if (browseCountLabel != null) {
                browseCountLabel.setText(browseData.size() + " books" +
                        (searchTerm.isEmpty() ? "" : " (filtered)"));
            }
        });
    }

    private void performBorrowSearch(String searchTerm) {
        if (allBooksList == null) return;

        Platform.runLater(() -> {
            List<Book> availableBooks = allBooksList.stream()
                    .filter(book -> book.getAvailable_copies() > 0)
                    .collect(Collectors.toList());

            if (searchTerm.isEmpty()) {
                borrowData.clear();
                borrowData.addAll(availableBooks);
                searchMessageLabel.setText("Showing all available books");
            } else {
                List<Book> searchResults = availableBooks.stream()
                        .filter(book -> {
                            boolean nameMatch = book.getName().toLowerCase().contains(searchTerm);
                            boolean authorMatch = book.getAuthorName().toLowerCase().contains(searchTerm);
                            boolean genreMatch = book.getGenre() != null &&
                                    book.getGenre().stream().anyMatch(genre ->
                                            genre.toLowerCase().contains(searchTerm));
                            return nameMatch || authorMatch || genreMatch;
                        })
                        .collect(Collectors.toList());

                borrowData.clear();
                borrowData.addAll(searchResults);
                searchMessageLabel.setText("Found " + searchResults.size() + " available book(s) matching '" + searchTerm + "'");
            }

            borrowTable.refresh();
        });
    }

    // Enhanced Borrow and Return Methods with better validation
    @FXML
    void handleRequestBorrow(ActionEvent event) throws IOException {
        String bookId = borrowBookIdField.getText().trim();
        if (bookId.isEmpty()) {
            showMessage(borrowMessageLabel, "⚠️ Please enter a Book ID or select from the table", "warning");
            return;
        }

        // Check if member can borrow more books
        if (currentMember != null && !currentMember.isMemberCanBorrow()) {
            showMessage(borrowMessageLabel, "❌ You have reached the borrow limit (5 books)", "error");
            return;
        }

        // Check if book exists and is available
        Book selectedBook = allBooksList.stream()
                .filter(book -> book.getBookId().equals(bookId))
                .findFirst()
                .orElse(null);

        if (selectedBook == null) {
            showMessage(borrowMessageLabel, "❌ Book with ID '" + bookId + "' not found", "error");
            return;
        }

        if (selectedBook.getAvailable_copies() <= 0) {
            showMessage(borrowMessageLabel, "❌ Book '" + selectedBook.getName() + "' is not available", "error");
            return;
        }

        // Check if member already has this book
        boolean alreadyBorrowed = myBooksData.stream()
                .anyMatch(book -> book.getBookId().equals(bookId));

        if (alreadyBorrowed) {
            showMessage(borrowMessageLabel, "⚠️ You have already borrowed this book", "warning");
            return;
        }

        SocketWrapper socketWrapper = Main.getSocketWrapper();
        MemberRequest memberRequest = new MemberRequest(bookId, currentMember.getUserId(), 1);
        socketWrapper.write(memberRequest);

        showMessage(borrowMessageLabel, "📝 Borrow request sent for: " + selectedBook.getName() + " (ID: " + bookId + ")", "success");
        borrowBookIdField.clear();
    }

    @FXML
    void handleRequestReturn(ActionEvent event) {
        String bookId = returnBookIdField.getText().trim();
        String ratingStr = ratingField.getText().trim();

        if (bookId.isEmpty()) {
            showMessage(returnMessageLabel, "⚠️ Please enter a Book ID or select from the table", "warning");
            return;
        }

        // Check if member actually has this book
        Book bookToReturn = myBooksData.stream()
                .filter(book -> book.getBookId().equals(bookId))
                .findFirst()
                .orElse(null);

        if (bookToReturn == null) {
            showMessage(returnMessageLabel, "❌ You don't have a book with ID: " + bookId, "error");
            return;
        }

        if (ratingStr.isEmpty()) {
            ratingStr = "0.0";
        }

        try {
            double rating = Double.parseDouble(ratingStr);
            if (rating < 0 || rating > 10) {
                showMessage(returnMessageLabel, "⚠️ Rating must be between 0 and 10", "warning");
                return;
            }

            SocketWrapper socketWrapper = Main.getSocketWrapper();
            MemberRequest memberRequest = new MemberRequest(bookId, currentMember.getUserId(), ratingStr);
            socketWrapper.write(memberRequest);

            showMessage(returnMessageLabel, "📤 Return request sent for: " + bookToReturn.getName() +
                    " (ID: " + bookId + ")" + (rating > 0 ? " with rating: " + rating + " ⭐" : ""), "success");
            returnBookIdField.clear();
            ratingField.clear();
        } catch (NumberFormatException e) {
            showMessage(returnMessageLabel, "❌ Invalid rating. Please enter a number between 0 and 10", "error");
        } catch (IOException e) {
            showMessage(returnMessageLabel, "❌ Error sending request. Please try again.", "error");
            e.printStackTrace();
        }
    }

    private void showMessage(Label messageLabel, String message, String type) {
        if (messageLabel == null) return;

        Platform.runLater(() -> {
            messageLabel.setText(message);
            switch (type) {
                case "success":
                    messageLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 13px; -fx-font-weight: 600;");
                    break;
                case "warning":
                    messageLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 13px; -fx-font-weight: 600;");
                    break;
                case "error":
                    messageLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 13px; -fx-font-weight: 600;");
                    break;
                default:
                    messageLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px; -fx-font-weight: 500;");
            }
        });
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        System.out.println("Manual refresh triggered for member");
        loadData();
        clearMessages();
        showMessage(searchMessageLabel != null ? searchMessageLabel : borrowMessageLabel,
                "🔄 Data refreshed successfully", "success");
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