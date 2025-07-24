package service;

import book.Book;

import java.util.List;

public class BookService {
    protected static List<Book> allBooks;
    public BookService(List<Book> allBooks){
        this.allBooks = allBooks;
    }
    public static Book findBook(String bookId){
        for(var x: allBooks){
            if(x.getBookId().equals(bookId)) return x;
        }
        return null;
    }
    public static void showAllBooks(){
        for(var x: allBooks){
            System.out.println(x);
        }
    }

    public static void addBookToSystem(Book book) {
        allBooks.add(book);
    }
    public static List<Book> getAllBooks() {
        return allBooks;
    }
}
