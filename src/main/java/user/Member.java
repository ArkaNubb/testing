package user;



import book.Book;

import java.io.Serializable;
import java.util.List;

public class Member extends user implements Serializable {

    private List<Book>borrowedBooks;
    private final int maxBooks=5;
    private int currentBooks;

    public Member(String email, String userId, String password, String name, List<Book>borrowedBooks) {
        super(email, userId, password, name);
        this.borrowedBooks = borrowedBooks;
        currentBooks=0;
    }


    public int getCurrentBooks(){
        return currentBooks;
    }

    public void addBorrowedBook(Book book){
        borrowedBooks.add(book);
        currentBooks++;
    }

    public void removeBook(Book book){
//        Book book = searchMyBook(bookname, authorName);
        borrowedBooks.remove(book);
    }
    public Book searchMyBook(String bookName, String authorName){
        for(var x:borrowedBooks){
            if(x.getName().equals(bookName) && x.getAuthorName().equals(authorName)){
                return x;
            }
        }
        return new Book();
    }
    public void showAllBorrowedBooks(){
        for(var x: borrowedBooks){
            System.out.println(x);
        }
    }
    public boolean isMemberCanBorrow(){
        if(borrowedBooks.size() < 5) return true;
        return false;
    }

    public List<Book> getBorrowedBooks() {
        return borrowedBooks;
    }

}
