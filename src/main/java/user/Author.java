package user;

import book.Book;

import java.util.List;

public class Author extends user{

    protected List<Book> publishedBooks;


    public Author(String email, String userId, String password, String name) {
        super(email, userId, password, name);
    }
    public String getName(){
        return name;
    }

    public void addPublishedBooks(Book book){
        publishedBooks.add(book);
    }

    public void removeBook(String bookname){
        for(var  x: publishedBooks){
            if(x.getName().equals(bookname)){
                publishedBooks.remove(x);
                return;
            }
        }
    }

}
