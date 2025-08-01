//package service;
//
//import common.Book;
//import common.Author;
//import common.Librarian;
//import common.Member;
//
//import java.io.*;
//import java.util.List;
//
//public class service {
//    protected static List<Book>allBooks;
//    protected static List<Member>allMembers;
//    protected static List<Author>allAuthors;
//    protected Librarian current_librarian;
//
//    protected BookService bookService;
//    protected MemberService memberService;
//    protected AuthorService authorService;
//    protected LibrarianService librarianService;
//
//    public service(List<Book> allBooks) {
//        this.allBooks = allBooks;
//    }
//
//    public service(BookService bookService, MemberService memberService, AuthorService authorService, LibrarianService librarianService) {
//        this.bookService = bookService;
//        this.memberService = memberService;
//        this.authorService = authorService;
//        this.librarianService = librarianService;
//    }
//
//    public void addMembers(List<Member>allMembers){
//        this.allMembers = allMembers;
//    }
//    public void addLibrarian(Librarian librarian){
//        this.current_librarian = librarian;
//    }
//    public void addAuthors(List<Author> allAuthors){
//        this.allAuthors = allAuthors;
//    }
//
//    public static Member isMemberFound(String userId){
//        for(var member: allMembers){
//            if(member.getUserId().equals(userId)) return member;
//        }
//        return null;
//    }
//    public void printMember(){
//        for(var x: allMembers){
//            System.out.println(x);
//        }
//    }
//    public void showAllBooks(){
//        for(var x: allBooks){
//            System.out.println(x);
//        }
//    }
//    public String generateMemberUserId(){
//        Member lastMember = allMembers.get(allMembers.size() - 1);
//        int userId = Integer.parseInt(lastMember.getUserId());
//        return String.valueOf((userId + 1));
//    }
//    public void addMember(Member member) throws IOException {
//        allMembers.add(member);
//        BufferedWriter memberInformation = new BufferedWriter(new FileWriter("src\\main\\java\\Main\\memberInformation.txt", true));
//        memberInformation.write("\n" + member.getEmail() + "|" + member.getUserId() + "|" + member.getPassword() + "|" + member.getName() + "|" + "dummybook");
//        memberInformation.close();
//    }
//    public static Book findBook(String bookId){
//        for(var x: allBooks){
//            if(x.getBookId().equals(bookId)) return x;
//        }
//        return null;
//    }
//    public void requestBorrowedBook(Librarian current_librarian, Member member, String bookId) throws IOException {
//        current_librarian.addPendingIssuingBook(member, bookId);
//    }
//
//    public void returnBorrowedBook(Librarian current_librarian, Member member, String bookId, double rating) throws IOException {
//        current_librarian.addRetunedBook(member, bookId, rating);
//    }
//    // Add these methods to your service.java class:
//
//    public static List<Author> getAllAuthors() {
//        return allAuthors;
//    }
//
//    public void addBookToSystem(Book book) {
//        allBooks.add(book);
//    }
//
//    public Author findAuthor(String authorId) {
//        for(Author author : allAuthors) {
//            if(author.getUserId().equals(authorId)) {
//                return author;
//            }
//        }
//        return null;
//    }
//}
