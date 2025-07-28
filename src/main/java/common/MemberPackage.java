package common;

import book.Book;
import user.Member;

import java.io.Serializable;
import java.util.List;

public class MemberPackage implements Serializable {
    Member member;
    List<Book>allBooks;

    public Member getMember() {
        return member;
    }

    public List<Book> getAllBooks() {
        return allBooks;
    }

    public MemberPackage(Member member, List<Book> allBooks) {
        this.member = member;
        this.allBooks = allBooks;
    }
}
