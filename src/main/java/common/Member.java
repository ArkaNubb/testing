package common;

import java.io.Serializable;
import java.util.List;

public class Member extends user implements Serializable {

    private List<Book> borrowedBooks;
    private final int maxBooks = 5;
    private int currentBooks;

    public Member(String email, String userId, String password, String name, List<Book> borrowedBooks) {
        super(email, userId, password, name);
        this.borrowedBooks = borrowedBooks;
        this.currentBooks = borrowedBooks != null ? borrowedBooks.size() : 0;
    }

    public int getCurrentBooks() {
        return borrowedBooks != null ? borrowedBooks.size() : 0;
    }

    public void addBorrowedBook(Book book) {
        if (borrowedBooks != null) {
            borrowedBooks.add(book);
            currentBooks = borrowedBooks.size();
        }
    }

    public void removeBook(Book book) {
        if (borrowedBooks != null) {
            borrowedBooks.remove(book);
            currentBooks = borrowedBooks.size();
        }
    }

    public Book searchMyBook(String bookName, String authorName) {
        if (borrowedBooks == null) return null;

        for (Book book : borrowedBooks) {
            if (book.getName().equals(bookName) && book.getAuthorName().equals(authorName)) {
                return book;
            }
        }
        return null;
    }

    public Book searchMyBookById(String bookId) {
        if (borrowedBooks == null) return null;

        for (Book book : borrowedBooks) {
            if (book.getBookId().equals(bookId)) {
                return book;
            }
        }
        return null;
    }

    public void showAllBorrowedBooks() {
        if (borrowedBooks != null) {
            for (Book book : borrowedBooks) {
                System.out.println(book);
            }
        }
    }

    public boolean isMemberCanBorrow() {
        return borrowedBooks != null && borrowedBooks.size() < maxBooks;
    }

    public List<Book> getBorrowedBooks() {
        return borrowedBooks;
    }

    public void setBorrowedBooks(List<Book> borrowedBooks) {
        this.borrowedBooks = borrowedBooks;
        this.currentBooks = borrowedBooks != null ? borrowedBooks.size() : 0;
    }

    public int getMaxBooks() {
        return maxBooks;
    }
}