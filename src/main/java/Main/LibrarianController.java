package Main;

import common.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.server;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class LibrarianController implements Initializable {

    @FXML private Label welcomeLabel;

    // Debug labels
    @FXML private Label issueCountLabel;
    @FXML private Label returnCountLabel;
    @FXML private Label publishCountLabel;

    // Main dashboard buttons
    @FXML private Button issueBookButton;
    @FXML private Button acceptReturnButton;
    @FXML private Button approvePublishButton;

    // TableViews - these will be used in separate windows
    private TableView<IssueRequest> issueRequestsTable;
    private TableView<ReturnRequest> returnRequestsTable;
    private TableView<PublishRequest> publishRequestsTable;

    // ObservableLists as member variables
    private final ObservableList<IssueRequest> issueData = FXCollections.observableArrayList();
    private final ObservableList<ReturnRequest> returnData = FXCollections.observableArrayList();
    private final ObservableList<PublishRequest> publishData = FXCollections.observableArrayList();

    // References to opened windows
    private Stage issueBookStage;
    private Stage returnBookStage;
    private Stage publishBookStage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        user currentUser = SceneManager.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getName() + " (Librarian)");
        }

        // Set the controller instance in Main
        Main.setLibrarianController(this);

        // Load initial data
        loadData();
    }

    public void loadData() {
        Platform.runLater(() -> {
            System.out.println("=== Refreshing Librarian UI ===");

            LibrarianPackage librarianPackage = Main.getLibrarianPackage();
            if (librarianPackage == null) {
                System.out.println("ERROR: Librarian package is null. Cannot load data.");
                updateCountLabels(0, 0, 0);
                return;
            }

            // Clear previous data
            issueData.clear();
            returnData.clear();
            publishData.clear();

            int issueCount = 0;
            int returnCount = 0;
            int publishCount = 0;

            // Populate issue requests
            if (librarianPackage.pendingIssuingBook != null) {
                for (Map.Entry<Member, java.util.List<String>> entry : librarianPackage.pendingIssuingBook.entrySet()) {
                    Member member = entry.getKey();
                    java.util.List<String> bookIds = entry.getValue();

                    for (String bookId : bookIds) {
                        if (!bookId.equalsIgnoreCase("dummybook") && !bookId.trim().isEmpty()) {
                            Book book = librarianPackage.findBook(bookId);
                            String bookTitle = (book != null) ? book.getName() : "Unknown Book";

                            IssueRequest request = new IssueRequest(
                                    member.getName(),
                                    member.getUserId(),
                                    bookTitle,
                                    bookId
                            );
                            issueData.add(request);
                            issueCount++;
                        }
                    }
                }
            }

            // Populate return requests
            if (librarianPackage.pendingReturnedBook != null) {
                for (Map.Entry<Member, java.util.List<Pair<String, Double>>> entry : librarianPackage.pendingReturnedBook.entrySet()) {
                    Member member = entry.getKey();
                    java.util.List<Pair<String, Double>> pairs = entry.getValue();

                    for (Pair<String, Double> pair : pairs) {
                        if (!pair.first.equalsIgnoreCase("dummybook") && !pair.first.trim().isEmpty()) {
                            Book book = librarianPackage.findBook(pair.first);
                            String bookTitle = (book != null) ? book.getName() : "Unknown Book";

                            ReturnRequest request = new ReturnRequest(
                                    member.getName(),
                                    member.getUserId(),
                                    bookTitle,
                                    pair.first,
                                    pair.second
                            );
                            returnData.add(request);
                            returnCount++;
                        }
                    }
                }
            }

            // Populate publish requests
            if (librarianPackage.pendingPublishRequests != null) {
                for (Map.Entry<Author, java.util.List<Book>> entry : librarianPackage.pendingPublishRequests.entrySet()) {
                    Author author = entry.getKey();
                    java.util.List<Book> books = entry.getValue();

                    for (Book book : books) {
                        if (!book.getName().equalsIgnoreCase("dummybook") && !book.getName().trim().isEmpty()) {
                            PublishRequest request = new PublishRequest(
                                    author.getName(),
                                    author.getUserId(),
                                    book.getName(),
                                    book.getBookId()
                            );
                            publishData.add(request);
                            publishCount++;
                        }
                    }
                }
            }

            // Update count labels
            updateCountLabels(issueCount, returnCount, publishCount);

            System.out.println("=== UI Update Complete ===");
            System.out.println("Issue requests loaded: " + issueCount);
            System.out.println("Return requests loaded: " + returnCount);
            System.out.println("Publish requests loaded: " + publishCount);
        });
    }

    private void updateCountLabels(int issueCount, int returnCount, int publishCount) {
        if (issueCountLabel != null) {
            issueCountLabel.setText("Pending Issue Requests: " + issueCount);
        }
        if (returnCountLabel != null) {
            returnCountLabel.setText("Pending Return Requests: " + returnCount);
        }
        if (publishCountLabel != null) {
            publishCountLabel.setText("Pending Publish Requests: " + publishCount);
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        System.out.println("Manual refresh triggered");
        loadData();
    }

    // Button handlers for main dashboard
    @FXML
    void handleIssueBookWindow(ActionEvent event) {
        if (issueBookStage != null && issueBookStage.isShowing()) {
            issueBookStage.toFront();
            return;
        }

        issueBookStage = createIssueBookWindow();
        issueBookStage.show();
    }

    @FXML
    void handleAcceptReturnWindow(ActionEvent event) {
        if (returnBookStage != null && returnBookStage.isShowing()) {
            returnBookStage.toFront();
            return;
        }

        returnBookStage = createReturnBookWindow();
        returnBookStage.show();
    }

    @FXML
    void handleApprovePublishWindow(ActionEvent event) {
        if (publishBookStage != null && publishBookStage.isShowing()) {
            publishBookStage.toFront();
            return;
        }

        publishBookStage = createPublishBookWindow();
        publishBookStage.show();
    }

    // Create Issue Book Window
    private Stage createIssueBookWindow() {
        Stage stage = new Stage();
        stage.setTitle("Issue Book Requests");
        stage.initModality(Modality.NONE);
        stage.setWidth(900);
        stage.setHeight(600);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        header.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 15; -fx-background-radius: 8;");

        Label titleLabel = new Label("📚 Pending Issue Requests");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #1976d2;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button backButton = new Button("← Back to Dashboard");
        backButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16 8 16; -fx-background-radius: 5;");
        backButton.setOnAction(e -> stage.close());

        header.getChildren().addAll(titleLabel, spacer, backButton);

        // Create table
        issueRequestsTable = new TableView<>();
        issueRequestsTable.setItems(issueData);

        TableColumn<IssueRequest, String> memberNameCol = new TableColumn<>("Member Name");
        memberNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMemberName()));
        memberNameCol.setPrefWidth(180);

        TableColumn<IssueRequest, String> memberIdCol = new TableColumn<>("Member ID");
        memberIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMemberId()));
        memberIdCol.setPrefWidth(120);

        TableColumn<IssueRequest, String> bookTitleCol = new TableColumn<>("Book Title");
        bookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookTitle()));
        bookTitleCol.setPrefWidth(350);

        TableColumn<IssueRequest, String> bookIdCol = new TableColumn<>("Book ID");
        bookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
        bookIdCol.setPrefWidth(120);

        issueRequestsTable.getColumns().addAll(memberNameCol, memberIdCol, bookTitleCol, bookIdCol);
        issueRequestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button approveButton = new Button("📋 Process Request & Approve");
        approveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 24 12 24; -fx-background-radius: 8;");
        approveButton.setPrefWidth(250);
        approveButton.setOnAction(e -> {
            try {
                handleApproveIssueFromWindow();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        buttonBox.getChildren().add(approveButton);

        root.getChildren().addAll(header, issueRequestsTable, buttonBox);
        VBox.setVgrow(issueRequestsTable, javafx.scene.layout.Priority.ALWAYS);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        return stage;
    }

    // Create Return Book Window
    private Stage createReturnBookWindow() {
        Stage stage = new Stage();
        stage.setTitle("Accept Book Returns");
        stage.initModality(Modality.NONE);
        stage.setWidth(950);
        stage.setHeight(600);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        header.setStyle("-fx-background-color: #e8f5e8; -fx-padding: 15; -fx-background-radius: 8;");

        Label titleLabel = new Label("📖 Pending Return Requests");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #2e7d32;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button backButton = new Button("← Back to Dashboard");
        backButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16 8 16; -fx-background-radius: 5;");
        backButton.setOnAction(e -> stage.close());

        header.getChildren().addAll(titleLabel, spacer, backButton);

        // Create table
        returnRequestsTable = new TableView<>();
        returnRequestsTable.setItems(returnData);

        TableColumn<ReturnRequest, String> memberNameCol = new TableColumn<>("Member Name");
        memberNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMemberName()));
        memberNameCol.setPrefWidth(160);

        TableColumn<ReturnRequest, String> memberIdCol = new TableColumn<>("Member ID");
        memberIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMemberId()));
        memberIdCol.setPrefWidth(110);

        TableColumn<ReturnRequest, String> bookTitleCol = new TableColumn<>("Book Title");
        bookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookTitle()));
        bookTitleCol.setPrefWidth(300);

        TableColumn<ReturnRequest, String> bookIdCol = new TableColumn<>("Book ID");
        bookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
        bookIdCol.setPrefWidth(110);

        TableColumn<ReturnRequest, Number> ratingCol = new TableColumn<>("Rating ⭐");
        ratingCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getRating()));
        ratingCol.setPrefWidth(90);

        returnRequestsTable.getColumns().addAll(memberNameCol, memberIdCol, bookTitleCol, bookIdCol, ratingCol);
        returnRequestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button acceptButton = new Button("✅ Review Member & Accept Return");
        acceptButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 24 12 24; -fx-background-radius: 8;");
        acceptButton.setPrefWidth(280);
        acceptButton.setOnAction(e -> {
            try {
                handleAcceptReturnFromWindow();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        buttonBox.getChildren().add(acceptButton);

        root.getChildren().addAll(header, returnRequestsTable, buttonBox);
        VBox.setVgrow(returnRequestsTable, javafx.scene.layout.Priority.ALWAYS);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        return stage;
    }

    // Create Publish Book Window
    private Stage createPublishBookWindow() {
        Stage stage = new Stage();
        stage.setTitle("Approve Book Publications");
        stage.initModality(Modality.NONE);
        stage.setWidth(900);
        stage.setHeight(600);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        header.setStyle("-fx-background-color: #fff3e0; -fx-padding: 15; -fx-background-radius: 8;");

        Label titleLabel = new Label("✍️ Pending Publish Requests");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #f57c00;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button backButton = new Button("← Back to Dashboard");
        backButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16 8 16; -fx-background-radius: 5;");
        backButton.setOnAction(e -> stage.close());

        header.getChildren().addAll(titleLabel, spacer, backButton);

        // Create table
        publishRequestsTable = new TableView<>();
        publishRequestsTable.setItems(publishData);

        TableColumn<PublishRequest, String> authorNameCol = new TableColumn<>("Author Name");
        authorNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthorName()));
        authorNameCol.setPrefWidth(180);

        TableColumn<PublishRequest, String> authorIdCol = new TableColumn<>("Author ID");
        authorIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthorId()));
        authorIdCol.setPrefWidth(120);

        TableColumn<PublishRequest, String> bookTitleCol = new TableColumn<>("Book Title");
        bookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookTitle()));
        bookTitleCol.setPrefWidth(350);

        TableColumn<PublishRequest, String> bookIdCol = new TableColumn<>("Book ID");
        bookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
        bookIdCol.setPrefWidth(120);

        publishRequestsTable.getColumns().addAll(authorNameCol, authorIdCol, bookTitleCol, bookIdCol);
        publishRequestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button approveButton = new Button("👤 Review Author & Approve Publication");
        approveButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 24 12 24; -fx-background-radius: 8;");
        approveButton.setPrefWidth(320);
        approveButton.setOnAction(e -> {
            try {
                handleApprovePublicationFromWindow();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        buttonBox.getChildren().add(approveButton);

        root.getChildren().addAll(header, publishRequestsTable, buttonBox);
        VBox.setVgrow(publishRequestsTable, javafx.scene.layout.Priority.ALWAYS);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        return stage;
    }

    // Handler methods for window-based actions
    private void handleApproveIssueFromWindow() throws IOException {
        IssueRequest selected = issueRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (showMemberInfoDialog(selected.getMemberId(), "Issue Book Approval")) {
                System.out.println("Approving issue request: " + selected.getMemberId() + " -> " + selected.getBookId());
                MemberRequest memberRequest = new MemberRequest(selected.getBookId(), selected.getMemberId(), 3);
                Main.getSocketWrapper().write(memberRequest);

                showSuccessAlert("Book Issue Approved",
                        "Book '" + selected.getBookTitle() + "' has been successfully issued to " + selected.getMemberName());

                // Refresh data and close window
                loadData();
                if (issueBookStage != null) {
                    issueBookStage.close();
                }
            }
        } else {
            showErrorAlert("No Selection", "Please select an issue request to approve.");
        }
    }

    private void handleAcceptReturnFromWindow() throws IOException {
        ReturnRequest selected = returnRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (showMemberInfoDialog(selected.getMemberId(), "Accept Book Return")) {
                System.out.println("Accepting return request: " + selected.getMemberId() + " -> " + selected.getBookId());
                MemberRequest memberRequest = new MemberRequest(selected.getBookId(), selected.getMemberId(), 4);
                Main.getSocketWrapper().write(memberRequest);

                showSuccessAlert("Book Return Accepted",
                        "Book '" + selected.getBookTitle() + "' return has been accepted from " + selected.getMemberName() +
                                " with rating: " + selected.getRating());

                // Refresh data and close window
                loadData();
                if (returnBookStage != null) {
                    returnBookStage.close();
                }
            }
        } else {
            showErrorAlert("No Selection", "Please select a return request to accept.");
        }
    }

    private void handleApprovePublicationFromWindow() throws IOException {
        PublishRequest selected = publishRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (showAuthorInfoDialog(selected.getAuthorId(), "Approve Book Publication")) {
                System.out.println("Approving publication request: " + selected.getAuthorId() + " -> " + selected.getBookId());
                server.librarianService.approvePublishRequest(selected.getAuthorId(), selected.getBookId());

                showSuccessAlert("Book Publication Approved",
                        "Book '" + selected.getBookTitle() + "' by " + selected.getAuthorName() + " has been approved for publication.");

                // Refresh data and close window
                loadData();
                if (publishBookStage != null) {
                    publishBookStage.close();
                }
            }
        } else {
            showErrorAlert("No Selection", "Please select a publication request to approve.");
        }
    }

    // Original handler methods (kept for compatibility)
    @FXML
    void handleApproveIssue(ActionEvent event) throws IOException {
        handleApproveIssueFromWindow();
    }

    @FXML
    void handleAcceptReturn(ActionEvent event) throws IOException {
        handleAcceptReturnFromWindow();
    }

    @FXML
    void handleApprovePublication(ActionEvent event) throws IOException {
        handleApprovePublicationFromWindow();
    }

    // Method to show member information dialog
    private boolean showMemberInfoDialog(String memberId, String actionTitle) {
        LibrarianPackage librarianPackage = Main.getLibrarianPackage();
        if (librarianPackage == null) return false;

        // Find the member
        Member member = null;
        for (Member m : librarianPackage.pendingIssuingBook.keySet()) {
            if (m.getUserId().equals(memberId)) {
                member = m;
                break;
            }
        }
        if (member == null) {
            for (Member m : librarianPackage.pendingReturnedBook.keySet()) {
                if (m.getUserId().equals(memberId)) {
                    member = m;
                    break;
                }
            }
        }

        if (member == null) {
            showErrorAlert("Error", "Member not found!");
            return false;
        }

        // Create dialog
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(actionTitle);
        dialog.setHeaderText("Member Information");

        // Create content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Member details
        grid.add(new Label("Name:"), 0, 0);
        grid.add(new Label(member.getName()), 1, 0);

        grid.add(new Label("Member ID:"), 0, 1);
        grid.add(new Label(member.getUserId()), 1, 1);

        grid.add(new Label("Email:"), 0, 2);
        grid.add(new Label(member.getEmail()), 1, 2);

        // Current borrowed books
        grid.add(new Label("Current Borrowed Books:"), 0, 3);
        if (member.getBorrowedBooks() != null && !member.getBorrowedBooks().isEmpty()) {
            StringBuilder borrowedBooks = new StringBuilder();
            for (Book book : member.getBorrowedBooks()) {
                borrowedBooks.append("• ").append(book.getName()).append(" (").append(book.getBookId()).append(")\n");
            }
            TextArea borrowedBooksArea = new TextArea(borrowedBooks.toString());
            borrowedBooksArea.setEditable(false);
            borrowedBooksArea.setPrefRowCount(3);
            borrowedBooksArea.setPrefColumnCount(30);
            grid.add(borrowedBooksArea, 1, 3);
        } else {
            grid.add(new Label("No books currently borrowed"), 1, 3);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(500, 350);

        Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // Method to show author information dialog
    private boolean showAuthorInfoDialog(String authorId, String actionTitle) {
        LibrarianPackage librarianPackage = Main.getLibrarianPackage();
        if (librarianPackage == null) return false;

        // Find the author
        Author author = null;
        for (Author a : librarianPackage.pendingPublishRequests.keySet()) {
            if (a.getUserId().equals(authorId)) {
                author = a;
                break;
            }
        }

        if (author == null) {
            showErrorAlert("Error", "Author not found!");
            return false;
        }

        // Create dialog
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(actionTitle);
        dialog.setHeaderText("Author Information");

        // Create content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Author details
        grid.add(new Label("Name:"), 0, 0);
        grid.add(new Label(author.getName()), 1, 0);

        grid.add(new Label("Author ID:"), 0, 1);
        grid.add(new Label(author.getUserId()), 1, 1);

        grid.add(new Label("Email:"), 0, 2);
        grid.add(new Label(author.getEmail()), 1, 2);

        // Published books
        grid.add(new Label("Previously Published Books:"), 0, 3);
        if (author.getPublishedBooks() != null && !author.getPublishedBooks().isEmpty()) {
            StringBuilder publishedBooks = new StringBuilder();
            for (Book book : author.getPublishedBooks()) {
                publishedBooks.append("• ").append(book.getName()).append(" (").append(book.getBookId()).append(")\n");
            }
            TextArea publishedBooksArea = new TextArea(publishedBooks.toString());
            publishedBooksArea.setEditable(false);
            publishedBooksArea.setPrefRowCount(3);
            publishedBooksArea.setPrefColumnCount(30);
            grid.add(publishedBooksArea, 1, 3);
        } else {
            grid.add(new Label("No books published yet"), 1, 3);
        }

        // Pending requests for this author
        grid.add(new Label("All Pending Requests:"), 0, 4);
        java.util.List<Book> pendingBooks = librarianPackage.pendingPublishRequests.get(author);
        if (pendingBooks != null && !pendingBooks.isEmpty()) {
            StringBuilder pendingList = new StringBuilder();
            for (Book book : pendingBooks) {
                if (!book.getName().equalsIgnoreCase("dummybook")) {
                    pendingList.append("• ").append(book.getName()).append(" (").append(book.getBookId()).append(")\n");
                    pendingList.append("  Genres: ").append(String.join(", ", book.getGenre())).append("\n");
                    pendingList.append("  Copies: ").append(book.getTotal_copies()).append("\n\n");
                }
            }
            TextArea pendingBooksArea = new TextArea(pendingList.toString());
            pendingBooksArea.setEditable(false);
            pendingBooksArea.setPrefRowCount(4);
            pendingBooksArea.setPrefColumnCount(30);
            grid.add(pendingBooksArea, 1, 4);
        } else {
            grid.add(new Label("No pending requests"), 1, 4);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(550, 450);

        Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // Helper method to show success alerts
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper method to show error alerts
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleLogout(ActionEvent event) throws IOException {
        // Close any open windows
        if (issueBookStage != null && issueBookStage.isShowing()) {
            issueBookStage.close();
        }
        if (returnBookStage != null && returnBookStage.isShowing()) {
            returnBookStage.close();
        }
        if (publishBookStage != null && publishBookStage.isShowing()) {
            publishBookStage.close();
        }

        // Send logout request to server to clean up client map
        if (SceneManager.getCurrentUser() != null) {
            SocketWrapper socketWrapper = Main.getSocketWrapper();
            if (socketWrapper != null) {
                LogoutRequest logoutRequest = new LogoutRequest(SceneManager.getCurrentUser().getUserId());
                socketWrapper.write(logoutRequest);
            }
        }

        // Clear the controller reference
        Main.setLibrarianController(null);

        // Clear the static data package to prevent using stale data on next login
        Main.setLibrarianPackage(null);

        // Clear the current user session
        SceneManager.setCurrentUser(null);

        // Go back to the login screen
        SceneManager.switchScene("/Main/login-view.fxml");
    }

    // Inner classes remain the same
    public static class IssueRequest {
        private final String memberName, memberId, bookTitle, bookId;
        public IssueRequest(String memberName, String memberId, String bookTitle, String bookId) {
            this.memberName = memberName;
            this.memberId = memberId;
            this.bookTitle = bookTitle;
            this.bookId = bookId;
        }
        public String getMemberName() { return memberName; }
        public String getMemberId() { return memberId; }
        public String getBookTitle() { return bookTitle; }
        public String getBookId() { return bookId; }
    }

    public static class ReturnRequest {
        private final String memberName, memberId, bookTitle, bookId;
        private final Double rating;
        public ReturnRequest(String memberName, String memberId, String bookTitle, String bookId, Double rating) {
            this.memberName = memberName;
            this.memberId = memberId;
            this.bookTitle = bookTitle;
            this.bookId = bookId;
            this.rating = rating;
        }
        public String getMemberName() { return memberName; }
        public String getMemberId() { return memberId; }
        public String getBookTitle() { return bookTitle; }
        public String getBookId() { return bookId; }
        public Double getRating() { return rating; }
    }

    public static class PublishRequest {
        private final String authorName, authorId, bookTitle, bookId;
        public PublishRequest(String authorName, String authorId, String bookTitle, String bookId) {
            this.authorName = authorName;
            this.authorId = authorId;
            this.bookTitle = bookTitle;
            this.bookId = bookId;
        }
        public String getAuthorName() { return authorName; }
        public String getAuthorId() { return authorId; }
        public String getBookTitle() { return bookTitle; }
        public String getBookId() { return bookId; }
    }
}