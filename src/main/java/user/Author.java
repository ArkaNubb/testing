package user;


import book.Book;
//import service.service;

import java.security.Provider;
import java.util.ArrayList;
import java.util.List;

public class Author extends user{

    protected List<Book> publishedBooks;

    public Author(String email, String userId, String password, String name, List<Book>publishedBooks){
        super(email,userId,password,name);
        this.publishedBooks = publishedBooks;
    }
    public String getName(){
        return name;
    }

    public void addPublishedBooks(Book book){
        publishedBooks.add(book);
    }

    public void removeBook(Book book){
//        for(int i=0; i<publishedBooks.size();i++){
//            if(publishedBooks.get(i).getName().equals(bookname)){
//                publishedBooks.remove(i);
//                return;
//            }
//        }
        publishedBooks.remove(book);
    }

    public List<Book> getPublishedBooks(){
        return publishedBooks;
    }

    public void showPublishedBooks(){
        if(publishedBooks.isEmpty()){
            System.out.println("No published books yet.");
            return;
        }
        System.out.println("Your Published Books:");
        for(Book book :publishedBooks){
            System.out.println(book);
        }
    }

    @Override
    public String toString(){
        return "Author{" +
                "name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", publishedBooks=" + publishedBooks.size() +
                '}';
    }
}
