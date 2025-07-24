package service;

import book.Book;
import user.Librarian;
import user.Member;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MemberService {
    Member member;
    protected static List<Member> allMembers;

    MemberService(Member member) {
        this.member = member;
    }

    public MemberService(List<Member> allMembers) {
        this.allMembers = allMembers;
    }

    //    public void requestBook(Book book, Librarian librarian){
//        librarian.addPendingIssuingBook(member, book);
//    }
//
//
//    public void requestReturnBook(Book book, Librarian librarian){
//        librarian.addRetunedBook(member, book);
//    }
//
//    public void rateBook(double rating,String bookname, String authorName){
//        Book book = member.searchMyBook(bookname, authorName);
//        book.setRating(rating);
//    }
    public static Member isMemberFound(String userId) {
        for (var member : allMembers) {
            if (member.getUserId().equals(userId)) return member;
        }
        return null;
    }

    public String generateMemberUserId() {
        Member lastMember = allMembers.get(allMembers.size() - 1);
        int userId = Integer.parseInt(lastMember.getUserId());
        return String.valueOf((userId + 1));
    }

    public void addMember(Member member) throws IOException, IOException {
        allMembers.add(member);
        BufferedWriter memberInformation = new BufferedWriter(new FileWriter("src\\main\\java\\Main\\memberInformation.txt", true));
        memberInformation.write("\n" + member.getEmail() + "|" + member.getUserId() + "|" + member.getPassword() + "|" + member.getName() + "|" + "dummybook");
        memberInformation.close();
    }
}