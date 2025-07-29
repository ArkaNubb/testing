package common;

import java.io.Serializable;
import java.util.List;

public class AuthorRequest implements Serializable {
    private String authorId;
    private String bookId;
    private int requestType;

    // For publish requests
    private String bookTitle;
    private String publishedDate;
    private List<String> genres;
    private int totalCopies;

    public static final int PUBLISH_BOOK = 1;
    public static final int REMOVE_BOOK = 2;
    public static final int APPROVE_PUBLICATION = 3;


    // Constructor for remove or approve book requests (by librarian)
    public AuthorRequest(String authorId, String bookId, int requestType) {
        this.authorId = authorId;
        this.bookId = bookId;
        this.requestType = requestType;
    }

    // UPDATED: Constructor for a NEW publish request (from author)
    // The server will generate the bookId, so it's removed from here.
    public AuthorRequest(String authorId, String bookTitle, String publishedDate,
                         List<String> genres, int totalCopies) {
        this.authorId = authorId;
        this.bookId = null; // The server will assign this
        this.bookTitle = bookTitle;
        this.publishedDate = publishedDate;
        this.genres = genres;
        this.totalCopies = totalCopies;
        this.requestType = PUBLISH_BOOK;
    }

    // Getters
    public String getAuthorId() { return authorId; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; } // Setter for server to use
    public int getRequestType() { return requestType; }
    public String getBookTitle() { return bookTitle; }
    public String getPublishedDate() { return publishedDate; }
    public List<String> getGenres() { return genres; }
    public int getTotalCopies() { return totalCopies; }
}