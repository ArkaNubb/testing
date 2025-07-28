package Main;

import book.Book;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import service.BookService;
import service.server;
import user.user;

import java.io.IOException;

public class LibrarianController {

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


    @FXML
    public void initialize() {
        user currentUser = SceneManager.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getName() + " (Librarian)");
        }
        setupTables();
        loadData();
    }

    private void setupTables() {
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
    }

    private void loadData() {
        ObservableList<IssueRequest> issueData = FXCollections.observableArrayList();
        server.librarianService.pendingIssuingBook.forEach((member, bookIds) -> {
            bookIds.stream()
                    .filter(id -> !id.equalsIgnoreCase("dummybook"))
                    .forEach(bookId -> {
                        Book book = BookService.findBook(bookId);
                        issueData.add(new IssueRequest(member.getName(), member.getUserId(), book != null ? book.getName() : "Unknown Book", bookId));
                    });
        });
        issueRequestsTable.setItems(issueData);

        ObservableList<ReturnRequest> returnData = FXCollections.observableArrayList();
        server.librarianService.pendingReturnedBook.forEach((member, pairs) -> {
            pairs.stream()
                    .filter(p -> !p.first.equalsIgnoreCase("dummybook"))
                    .forEach(pair -> {
                        Book book = BookService.findBook(pair.first);
                        returnData.add(new ReturnRequest(member.getName(), member.getUserId(), book != null ? book.getName() : "Unknown Book", pair.first, pair.second));
                    });
        });
        returnRequestsTable.setItems(returnData);

        ObservableList<PublishRequest> publishData = FXCollections.observableArrayList();
        server.librarianService.pendingPublishRequests.forEach((author, books) -> {
            books.stream()
                    .filter(b -> !b.getName().equalsIgnoreCase("dummybook"))
                    .forEach(book -> publishData.add(new PublishRequest(author.getName(), author.getUserId(), book.getName(), book.getBookId())));
        });
        publishRequestsTable.setItems(publishData);
    }

    @FXML void handleApproveIssue(ActionEvent event) throws IOException {
        IssueRequest selected = issueRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            server.librarianService.approveBook(selected.getMemberId(), selected.getBookId());
            loadData();
        }
    }

    @FXML void handleAcceptReturn(ActionEvent event) throws IOException {
        ReturnRequest selected = returnRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            server.librarianService.acceptBook(selected.getMemberId(), selected.getBookId());
            loadData();
        }
    }

    @FXML void handleApprovePublication(ActionEvent event) throws IOException {
        PublishRequest selected = publishRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            server.librarianService.approvePublishRequest(selected.getAuthorId(), selected.getBookId());
            loadData();
        }
    }

    @FXML void handleLogout(ActionEvent event) throws IOException {
        SceneManager.setCurrentUser(null);
        SceneManager.switchScene("/Main/login-view.fxml");
    }

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