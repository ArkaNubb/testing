package service;

import book.Book;
import user.Librarian;
import user.Member;

public class LibrarianService {
    Librarian librarian;

    public LibrarianService(Librarian librarian){
        this.librarian = librarian;
    }
    public void issueBook(Member member, Book book){
        member.addBorrowedBook(book);
        librarian.pendingIssuingBook.get(member).remove(book);
    }
    public void acceptRetunedBook(Member member, Book book){
        member.removeBook(book);
        librarian.pendingIssuingBook.get(member).remove(book);
    }

}
