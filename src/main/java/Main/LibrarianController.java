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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import service.server;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class LibrarianController implements Initializable {

    @FXML private Label welcomeLabel;

    // Debug labels
    @FXML private Label issueCountLabel;
    @FXML private Label returnCountLabel;
    @FXML private Label publishCountLabel;

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

    // ObservableLists as member variables
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

        // Set the controller instance in Main
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

        // Set table items once
        issueRequestsTable.setItems(issueData);
        returnRequestsTable.setItems(returnData);
        publishRequestsTable.setItems(publishData);

        // Set table properties for better scrolling
        issueRequestsTable.setRowFactory(tv -> {
            var row = new javafx.scene.control.TableRow<IssueRequest>();
            return row;
        });

        returnRequestsTable.setRowFactory(tv -> {
            var row = new javafx.scene.control.TableRow<ReturnRequest>();
            return row;
        });

        publishRequestsTable.setRowFactory(tv -> {
            var row = new javafx.scene.control.TableRow<PublishRequest>();
            return row;
        });
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
                System.out.println("Processing " + librarianPackage.pendingIssuingBook.size() + " members with pending issue requests");

                for (Map.Entry<Member, java.util.List<String>> entry : librarianPackage.pendingIssuingBook.entrySet()) {
                    Member member = entry.getKey();
                    java.util.List<String> bookIds = entry.getValue();

                    System.out.println("Member: " + member.getName() + " has " + bookIds.size() + " pending requests");

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

                            System.out.println("Added issue request: " + member.getName() + " -> " + bookTitle + " (" + bookId + ")");
                        }
                    }
                }
            }

            // Populate return requests
            if (librarianPackage.pendingReturnedBook != null) {
                System.out.println("Processing " + librarianPackage.pendingReturnedBook.size() + " members with return requests");

                for (Map.Entry<Member, java.util.List<Pair<String, Double>>> entry : librarianPackage.pendingReturnedBook.entrySet()) {
                    Member member = entry.getKey();
                    java.util.List<Pair<String, Double>> pairs = entry.getValue();

                    System.out.println("Member: " + member.getName() + " has " + pairs.size() + " return requests");

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

                            System.out.println("Added return request: " + member.getName() + " -> " + bookTitle + " (" + pair.first + ") Rating: " + pair.second);
                        }
                    }
                }
            }

            // Populate publish requests
            if (librarianPackage.pendingPublishRequests != null) {
                System.out.println("Processing " + librarianPackage.pendingPublishRequests.size() + " authors with publish requests");

                for (Map.Entry<Author, java.util.List<Book>> entry : librarianPackage.pendingPublishRequests.entrySet()) {
                    Author author = entry.getKey();
                    java.util.List<Book> books = entry.getValue();

                    System.out.println("Author: " + author.getName() + " has " + books.size() + " publish requests");

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

                            System.out.println("Added publish request: " + author.getName() + " -> " + book.getName() + " (" + book.getBookId() + ")");
                        }
                    }
                }
            }

            // Update count labels
            updateCountLabels(issueCount, returnCount, publishCount);

            // Force table refresh
            issueRequestsTable.refresh();
            returnRequestsTable.refresh();
            publishRequestsTable.refresh();

            System.out.println("=== UI Update Complete ===");
            System.out.println("Issue requests loaded: " + issueCount);
            System.out.println("Return requests loaded: " + returnCount);
            System.out.println("Publish requests loaded: " + publishCount);
        });
    }

    private void updateCountLabels(int issueCount, int returnCount, int publishCount) {
        if (issueCountLabel != null) {
            issueCountLabel.setText("Issue Requests: " + issueCount);
        }
        if (returnCountLabel != null) {
            returnCountLabel.setText("Return Requests: " + returnCount);
        }
        if (publishCountLabel != null) {
            publishCountLabel.setText("Publish Requests: " + publishCount);
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        System.out.println("Manual refresh triggered");
        loadData();
    }

    @FXML void handleApproveIssue(ActionEvent event) throws IOException {
        IssueRequest selected = issueRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Approving issue request: " + selected.getMemberId() + " -> " + selected.getBookId());
            MemberRequest memberRequest = new MemberRequest(selected.getBookId(), selected.getMemberId(), 3);
            Main.getSocketWrapper().write(memberRequest);
        } else {
            System.out.println("No issue request selected");
        }
    }

    @FXML void handleAcceptReturn(ActionEvent event) throws IOException {
        ReturnRequest selected = returnRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Accepting return request: " + selected.getMemberId() + " -> " + selected.getBookId());
            MemberRequest memberRequest = new MemberRequest(selected.getBookId(), selected.getMemberId(), 4);
            Main.getSocketWrapper().write(memberRequest);
        } else {
            System.out.println("No return request selected");
        }
    }

    // --- UPDATED METHOD ---
    @FXML void handleApprovePublication(ActionEvent event) throws IOException {
        PublishRequest selected = publishRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Sending publication approval request for: " + selected.getAuthorId() + " -> " + selected.getBookId());

            // CORRECTED: Create a request object and send it through the socket.
            AuthorRequest approvalRequest = new AuthorRequest(selected.getAuthorId(), selected.getBookId(), AuthorRequest.APPROVE_PUBLICATION);
            Main.getSocketWrapper().write(approvalRequest);

        } else {
            System.out.println("No publication request selected");
        }
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