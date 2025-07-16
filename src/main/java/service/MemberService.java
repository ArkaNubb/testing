package service;

import book.Book;
import user.Librarian;
import user.Member;

public class MemberService {
    Member member;

    MemberService(Member member){
        this.member = member;
    }

    public void requestBook(Book book, Librarian librarian){
        librarian.addPendingIssuingBook(member, book);
    }


    public void requestReturnBook(Book book, Librarian librarian){
        librarian.addRetunedBook(member, book);
    }

    public void rateBook(double rating,String bookname, String authorName){
        Book book = member.searchMyBook(bookname, authorName);
        book.setRating(rating);
    }




}
