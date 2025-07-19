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

}
