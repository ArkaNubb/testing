package service;

import book.Book;
import user.Author;

import java.util.List;

public class AuthorService {

    Author author;


    public AuthorService(Author author){
        this.author = author;
    }

    public void publishBook(String name, String publishedDate, String authorName, List<String> genre ){
        Book book=new Book(name,publishedDate,authorName,genre);
        author.addPublishedBooks(book);
    }

    public void removeBook(String bookname){
        author.removeBook(bookname);
    }



}
