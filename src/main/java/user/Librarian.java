package user;

import book.Book;

import java.util.List;
import java.util.Map;

public class Librarian extends user{

    private List<Book>issuedBooks;
    public Map<Member, List<Book>> pendingIssuingBook;
    public Map<Member, List<Book>> pendingReturnedBook;

    public Librarian(String email, String userId, String name, String password) {
        super(email, userId, name, password);
    }

    public void addPendingIssuingBook(Member member, Book book) {
        pendingIssuingBook.get(member).add(book);
    }
    public void addRetunedBook(Member member, Book book) {
        pendingReturnedBook.get(member).add(book);
    }

    @Override
    public void printInfo() {

    }
}
