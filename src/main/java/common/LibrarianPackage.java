package common;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class LibrarianPackage implements Serializable {
    Librarian librarian;
    public Map<Member, List<String>> pendingIssuingBook;
    public Map<Member, List<Pair<String, Double>>> pendingReturnedBook;
    public Map<Author, List<Book>> pendingPublishRequests;
    public List<Book>allBooks;


    public LibrarianPackage(Librarian librarian, Map<Member, List<String>> pendingIssuingBook, Map<Member, List<Pair<String, Double>>> pendingReturnedBook, Map<Author, List<Book>> pendingPublishRequests, List<Book>allBooks) {
        this.librarian = librarian;
        this.pendingIssuingBook = pendingIssuingBook;
        this.pendingReturnedBook = pendingReturnedBook;
        this.pendingPublishRequests = pendingPublishRequests;
        this.allBooks = allBooks;
    }

    public Librarian getLibrarian() {
        return librarian;
    }
    public Book findBook(String bookId){
        for (var x: allBooks){
            if(x.getBookId().equals(bookId)){
                return x;
            }
        }
        return null;
    }

}
