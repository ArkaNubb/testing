package service;

import book.Book;
import user.Author;
import user.Librarian;
import user.Member;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class service {
    protected static List<Book>allBooks;
    protected static List<Member>allMembers;
    protected List<Author>allAuthors;
    protected Librarian current_librarian;

    public service(List<Author> allAuthors, List<Book> allBooks) {
        this.allAuthors = allAuthors;
//        this.allMembers = allMembers;
        this.allBooks = allBooks;
    }
    public void addMembers(List<Member>allMembers){
        this.allMembers = allMembers;
    }
    public void addLibrarian(Librarian librarian){
        this.current_librarian = librarian;
    }


    public static Member isMemberFound(String userId){
        for(var member: allMembers){
            if(member.getUserId().equals(userId)) return member;
        }
        return null;
    }
    public void printMember(){
        for(var x: allMembers){
            System.out.println(x);
        }
    }
    public void showAllBooks(){
        for(var x: allBooks){
            System.out.println(x);
        }
    }
    public String generateMemberUserId(){
        Member lastMember = allMembers.get(allMembers.size() - 1);
        int userId = Integer.parseInt(lastMember.getUserId());
        return String.valueOf((userId + 1));
    }
    public void addMember(Member member) throws IOException {
        allMembers.add(member);
        BufferedWriter memberInformation = new BufferedWriter(new FileWriter("src\\main\\java\\Main\\memberInformation.txt", true));
        memberInformation.write("\n" + member.getEmail() + "|" + member.getUserId() + "|" + member.getPassword() + "|" + member.getName() + "|" + "dummybook");
        memberInformation.close();
    }
    public static Book findBook(String bookId){
        for(var x: allBooks){
//            if(x.equals("dummybook")) continue;
            if(x.getBookId().equals(bookId)) return x;
        }
        return null;
    }
    public void requestBorrowedBook(Librarian current_librarian, Member member, String bookId) throws IOException {
        current_librarian.addPendingIssuingBook(member, bookId);
    }

    public void returnBorrowedBook(Librarian current_librarian, Member member, String bookId, double rating) throws IOException {
        current_librarian.addRetunedBook(member, bookId, rating);
    }

}
