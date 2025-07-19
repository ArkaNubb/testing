package service;

import book.Book;
import user.Author;
import user.Librarian;
import user.Member;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class service {
    protected List<Book>allBooks;
    protected List<Member>allMembers;
    protected List<Author>allAuthors;
    protected Librarian current_librarian;

    public service(Librarian current_librarian, List<Author> allAuthors, List<Member> allMembers, List<Book> allBooks) {
        this.current_librarian = current_librarian;
        this.allAuthors = allAuthors;
        this.allMembers = allMembers;
        this.allBooks = allBooks;
    }
}
