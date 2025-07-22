package service;

import book.Book;
import user.Author;
import user.Librarian;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AuthorService {

    Author author;

    public AuthorService(Author author){
        this.author = author;
    }

    public void requestPublishBook(String name,String publishedDate,List<String>genre,int totalCopies,Librarian librarian) throws IOException{
        String bookId = generateBookId();
        // with default rating currently 0.0
        List<Double> defaultRatings=new ArrayList<>();
        Book book=new Book(name, bookId, publishedDate, author.getName(), genre, defaultRatings, totalCopies, totalCopies);

        // eequesting librarian to approve the publishing
        librarian.addPendingPublishRequest(author, book);

        System.out.println("book publishing request sent to librarian :");
        System.out.println("Book ID: " + bookId);
        System.out.println("wait for librarian approval.");
    }

    public void RemoveBook(String bookname){
        author.removeBook(bookname);
    }

    public void showPublishedBooks(){
        author.showPublishedBooks();
    }

    private String generateBookId() {
        return String.valueOf(20000 + (int)(Math.random() * 10000));
    }
}