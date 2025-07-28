package Main;

import common.Book;
import common.LibrarianPackage;
import common.user;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import service.server;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LibrarianController implements Initializable {

    @FXML private Label welcomeLabel;

    @FXML private TableView<IssueRequest> issueRequestsTable;
    @FXML private TableColumn<IssueRequest, String> issueMemberNameCol;
    @FXML private TableColumn<IssueRequest, String> issueMemberIdCol;
    @FXML private TableColumn<IssueRequest, String> issueBookTitleCol;
    @FXML private TableColumn<IssueRequest, String> issueBookIdCol;

    @FXML private TableView<ReturnRequest> returnRequestsTable;
    @FXML private TableColumn<ReturnRequest, String> returnMemberNameCol;
    @FXML private TableColumn<ReturnRequest, String> returnMemberIdCol;
    @FXML private TableColumn<ReturnRequest, String> returnBookTitleCol;
    @FXML private TableColumn<ReturnRequest, String> returnBookIdCol;
    @FXML private TableColumn<ReturnRequest, Number> returnRatingCol;

    @FXML private TableView<PublishRequest> publishRequestsTable;
    @FXML private TableColumn<PublishRequest, String> publishAuthorNameCol;
    @FXML private TableColumn<PublishRequest, String> publishAuthorIdCol;
    @FXML private TableColumn<PublishRequest, String> publishBookTitleCol;
    @FXML private TableColumn<PublishRequest, String> publishBookIdCol;

    // --- MODIFICATION 1: Declare ObservableLists as member variables ---
    private final ObservableList<IssueRequest> issueData = FXCollections.observableArrayList();
    private final ObservableList<ReturnRequest> returnData = FXCollections.observableArrayList();
    private final ObservableList<PublishRequest> publishData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        user currentUser = SceneManager.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getName() + " (Librarian)");
        }
        setupTables();

        // --- MODIFICATION 2: Set the controller instance in Main ---
        Main.setLibrarianController(this);

        // Load initial data
        loadData();
    }

    private void setupTables() {
        // Set cell value factories
        issueMemberNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMemberName()));
        issueMemberIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMemberId()));
        issueBookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookTitle()));
        issueBookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));

        returnMemberNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMemberName()));
        returnMemberIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMemberId()));
        returnBookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookTitle()));
        returnBookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));
        returnRatingCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getRating()));

        publishAuthorNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthorName()));
        publishAuthorIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthorId()));
        publishBookTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookTitle()));
        publishBookIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookId()));

        // --- MODIFICATION 3: Set table items once ---
        issueRequestsTable.setItems(issueData);
        returnRequestsTable.setItems(returnData);
        publishRequestsTable.setItems(publishData);
    }

    // --- MODIFICATION 4: Update loadData to be thread-safe and refresh existing lists ---
    public void loadData() {
        Platform.runLater(() -> {
            System.out.println("Refreshing Librarian UI...");

            // Clear previous data
            issueData.clear();
            returnData.clear();
            publishData.clear();

            LibrarianPackage librarianPackage = Main.getLibrarianPackage();
            if (librarianPackage == null) {
                System.out.println("Librarian package is null. Cannot load data.");
                return;
            }

            // Populate issue requests
            if (librarianPackage.pendingIssuingBook != null) {
                librarianPackage.pendingIssuingBook.forEach((member, bookIds) -> {
                    bookIds.stream()
                            .filter(id -> !id.equalsIgnoreCase("dummybook"))
                            .forEach(bookId -> {
                                Book book = librarianPackage.findBook(bookId);
                                issueData.add(new IssueRequest(member.getName(), member.getUserId(), book != null ? book.getName() : "Unknown Book", bookId));
                            });
                });
            }

            // Populate return requests
            if (librarianPackage.pendingReturnedBook != null) {
                librarianPackage.pendingReturnedBook.forEach((member, pairs) -> {
                    pairs.stream()
                            .filter(p -> !p.first.equalsIgnoreCase("dummybook"))
                            .forEach(pair -> {
                                Book book = librarianPackage.findBook(pair.first);
                                returnData.add(new ReturnRequest(member.getName(), member.getUserId(), book != null ? book.getName() : "Unknown Book", pair.first, pair.second));
                            });
                });
            }

            // Populate publish requests
            if (librarianPackage.pendingPublishRequests != null) {
                librarianPackage.pendingPublishRequests.forEach((author, books) -> {
                    books.stream()
                            .filter(b -> !b.getName().equalsIgnoreCase("dummybook"))
                            .forEach(book -> publishData.add(new PublishRequest(author.getName(), author.getUserId(), book.getName(), book.getBookId())));
                });
            }
            System.out.println("Librarian UI updated.");
        });
    }

    @FXML void handleApproveIssue(ActionEvent event) throws IOException {
        IssueRequest selected = issueRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // This action on the server will trigger a broadcast, which will cause this client to refresh
            server.librarianService.approveBook(selected.getMemberId(), selected.getBookId());
        }
    }

    @FXML void handleAcceptReturn(ActionEvent event) throws IOException {
        ReturnRequest selected = returnRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            server.librarianService.acceptBook(selected.getMemberId(), selected.getBookId());
        }
    }

    @FXML void handleApprovePublication(ActionEvent event) throws IOException {
        PublishRequest selected = publishRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            server.librarianService.approvePublishRequest(selected.getAuthorId(), selected.getBookId());
        }
    }

    @FXML void handleLogout(ActionEvent event) throws IOException {
            // Clear the controller reference
            Main.setLibrarianController(null);

            // --- ADD THIS LINE ---
            // Clear the static data package to prevent using stale data on next login
            Main.setLibrarianPackage(null);

            // Clear the current user session
            SceneManager.setCurrentUser(null);

            // Go back to the login screen
            SceneManager.switchScene("/Main/login-view.fxml");
    }

    // --- Inner classes remain the same ---
    public static class IssueRequest {
        private final String memberName, memberId, bookTitle, bookId;
        public IssueRequest(String memberName, String memberId, String bookTitle, String bookId) { this.memberName = memberName; this.memberId = memberId; this.bookTitle = bookTitle; this.bookId = bookId; }
        public String getMemberName() { return memberName; }
        public String getMemberId() { return memberId; }
        public String getBookTitle() { return bookTitle; }
        public String getBookId() { return bookId; }
    }

    public static class ReturnRequest {
        private final String memberName, memberId, bookTitle, bookId;
        private final Double rating;
        public ReturnRequest(String memberName, String memberId, String bookTitle, String bookId, Double rating) { this.memberName = memberName; this.memberId = memberId; this.bookTitle = bookTitle; this.bookId = bookId; this.rating = rating; }
        public String getMemberName() { return memberName; }
        public String getMemberId() { return memberId; }
        public String getBookTitle() { return bookTitle; }
        public String getBookId() { return bookId; }
        public Double getRating() { return rating; }
    }

    public static class PublishRequest {
        private final String authorName, authorId, bookTitle, bookId;
        public PublishRequest(String authorName, String authorId, String bookTitle, String bookId) { this.authorName = authorName; this.authorId = authorId; this.bookTitle = bookTitle; this.bookId = bookId; }
        public String getAuthorName() { return authorName; }
        public String getAuthorId() { return authorId; }
        public String getBookTitle() { return bookTitle; }
        public String getBookId() { return bookId; }
    }
}