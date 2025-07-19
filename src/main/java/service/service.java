package service;

import book.Book;
import user.Author;
import user.Librarian;
import user.Member;

import java.io.*;
import java.util.List;

public class service {
    protected List<Book>allBooks;
    protected List<Member>allMembers;
    protected List<Author>allAuthors;
    protected Librarian current_librarian;

    public service(Librarian current_librarian, List<Author> allAuthors, List<Member> allMembers, List<Book> allBooks) {
        this.current_librarian = current_librarian;
        this.allAuthors = allAuthors;
        this.allMembers = allMembers;
        this.allBooks = allBooks;
    }
    public Member isMemberFound(String userId){
        for(var member: allMembers){
            if(member.getUserId().equals(userId)) return member;
        }
        return null;
    }
//    public boolean isMemberPasswordMatched(String password){
//        for(var member: allMembers){
//            if(member.getPassword() == password) return true;
//        }
//        return false;
//    }
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
        BufferedWriter memberInformation = new BufferedWriter(new FileWriter("D:\\CSE\\1-2 project\\testing\\src\\main\\java\\Main\\memberInformation.txt", true));
        memberInformation.write("\n" + member.getEmail() + "|" + member.getUserId() + "|" + member.getPassword() + "|" + member.getName());
        memberInformation.close();
    }

}
