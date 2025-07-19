package user;

import book.Book;
import service.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Librarian extends user{

//    private List<Book>issuedBooks;
    public Map<Member, List<String>> pendingIssuingBook;
    public Map<Member, List<String>> pendingReturnedBook;

    public Librarian(String email, String userId, String password, String name) {
        super(email, userId, password, name);
        pendingIssuingBook = new HashMap<>();
        pendingReturnedBook = new HashMap<>();
    }
    public void addPendingIssuingBook(Member member, String bookId) {
        if(pendingIssuingBook.containsKey(member)){
            pendingIssuingBook.get(member).add(bookId);
        }
        else{
            List<String> list = new ArrayList<>();
            list.add(bookId);
            pendingIssuingBook.put(member, list);
        }

    }
    public void addRetunedBook(Member member, String bookId) {
        if(pendingReturnedBook.containsKey(member)){
            pendingReturnedBook.get(member).add(bookId);
        }
        else{
            List<String> list = new ArrayList<>();
            list.add(bookId);
            pendingReturnedBook.put(member, list);
        }
    }
    public void showPendingBooks(service serve){
        for(Member member: pendingIssuingBook.keySet()){
            System.out.print("Name: ");
            System.out.print(member.getName());
            System.out.print(", Id: ");
            System.out.println(member.getUserId());
            System.out.println("Pending Books: ");
            for(var bookId: pendingIssuingBook.get(member)){
                Book book = serve.findBook(bookId);
                System.out.print("Name: ");
                System.out.print(book.getName());
                System.out.print(", Id: ");
                System.out.println(book.getBookId());
            }
        }
    }
    public void showPendingReturnBook(service serve){
        for(Member member: pendingReturnedBook.keySet()){
            System.out.print("Name: ");
            System.out.print(member.getName());
            System.out.print(", Id: ");
            System.out.println(member.getUserId());
            System.out.println("Pending Books: ");
            for(var bookId: pendingReturnedBook.get(member)){
                Book book = serve.findBook(bookId);
                System.out.print("Name: ");
                System.out.print(book.getName());
                System.out.print(", Id: ");
                System.out.println(book.getBookId());
            }
        }
    }
    public void approveBook(service serve, String memberUserId, String bookId) throws IOException {
        Member member = serve.isMemberFound(memberUserId);
        member.addBorrowedBook(serve.findBook(bookId));
        String filePath = "src\\main\\java\\Main\\memberInformation.txt";
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        boolean found = false;
        for (int i = 0; i < lines.size(); i++){
            if(lines.get(i).contains("|" + member.getUserId()+"|")){
                found = true;
                String[] parts = lines.get(i).split("\\|");
                String[] books = parts[4].split(",");

                List<String> bookList = new ArrayList<>(Arrays.asList(books));
                String updatedBooks = String.join(",", bookList);
                if (!bookId.isEmpty()) {
                    updatedBooks += (updatedBooks.isEmpty() ? "" : ",") + bookId;
                }
                parts[4] = updatedBooks;
                lines.set(i, String.join("|", parts));
                break;
            }
        }
        Files.write(Paths.get(filePath), lines);
    }
    public void acceptBook(service serve, String memberUserId, String bookId) throws IOException{
        Member member = serve.isMemberFound(memberUserId);
        member.removeBook(serve.findBook(bookId));
        String filePath = "src\\main\\java\\Main\\memberInformation.txt";
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        boolean found = false;
        for (int i = 0; i < lines.size(); i++){
            if(lines.get(i).contains("|" + member.getUserId()+"|")){
                found = true;
                String[] parts = lines.get(i).split("\\|");
                String[] books = parts[4].split(",");

                List<String> bookList = new ArrayList<>(Arrays.asList(books));
                bookList.remove(bookId);
                String updatedBooks = String.join(",", bookList);
                parts[4] = updatedBooks;
                lines.set(i, String.join("|", parts));
                break;
            }
        }
        Files.write(Paths.get(filePath), lines);
    }
}
