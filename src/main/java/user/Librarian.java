package user;

import book.Book;
import javafx.scene.Parent;
import service.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class Pair<K, V>{
    public K first;
    public V second;

    public Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}


public class Librarian extends user{

//    private List<Book>issuedBooks;
    public Map<Member, List<String>> pendingIssuingBook;
    public Map<Member, List<Pair<String, Double>>> pendingReturnedBook;

    public Librarian(String email, String userId, String password, String name) throws IOException {
        super(email, userId, password, name);
        pendingIssuingBook = new HashMap<>();
        pendingReturnedBook = new HashMap<>();
        BufferedReader readPendingBooks =  new BufferedReader(new FileReader("src\\main\\java\\Main\\librarianPendingBooks.txt"));
        BufferedReader readPendingReturnBooks =  new BufferedReader(new FileReader("src\\main\\java\\Main\\librarianPendingReturnBooks.txt"));

        // reading pending Books
        List<String>pendingbookList = new ArrayList<>();
        String str;
        while((str = readPendingBooks.readLine()) != null){
            str = str.trim();
            if(str.isEmpty()) continue;
            pendingbookList.add(str);
        }
        for (var x: pendingbookList){
            String[] values = x.split("\\|");
            Member member = service.isMemberFound(values[0]);
            List<String> bookIds = new ArrayList<>();
            String[] vv = values[1].split(",");
            for(var v: vv){
                bookIds.add(v);
            }
            pendingIssuingBook.put(member, bookIds);
        }

        // reading pending return books
        List<String>pendingbookreturnList = new ArrayList<>();
        while((str = readPendingReturnBooks.readLine()) != null){
            str = str.trim();
            if(str.isEmpty()) continue;
            pendingbookreturnList.add(str);
        }
        for (var x: pendingbookreturnList){
            String[] values = x.split("\\|");
            Member member = service.isMemberFound(values[0]);
            List<String> bookIds = new ArrayList<>();
            String[] vv = values[1].split(",");
            for(var v: vv){
                bookIds.add(v);
            }
            pendingReturnedBook.put(member, bookIds);
        }
    }
    public void addPendingIssuingBook(Member member, String bookId) throws IOException {
        if(pendingIssuingBook.containsKey(member)){
            pendingIssuingBook.get(member).add(bookId);
            String filePath = "src\\main\\java\\Main\\librarianPendingBooks.txt";
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            boolean found = false;
            for (int i = 0; i < lines.size(); i++){
                if(lines.get(i).contains(member.getUserId()+"|")){
                    found = true;
                    String[] parts = lines.get(i).split("\\|");
                    String[] books = parts[1].split(",");

                    List<String> bookList = new ArrayList<>(Arrays.asList(books));
                    String updatedBooks = String.join(",", bookList);
                    if (!bookId.isEmpty()) {
                        updatedBooks += (updatedBooks.isEmpty() ? "" : ",") + bookId;
                    }
                    parts[1] = updatedBooks;
                    lines.set(i, String.join("|", parts));
                    break;
                }
            }
            Files.write(Paths.get(filePath), lines);
        }
        else{
            List<String> list = new ArrayList<>();
            list.add(bookId);
            pendingIssuingBook.put(member, list);
            BufferedWriter pendingInformation = new BufferedWriter(new FileWriter("src\\main\\java\\Main\\librarianPendingBooks.txt", true));
            pendingInformation.write( member.getUserId() + "|" + "dummybook," + bookId + "\n");
            pendingInformation.close();
        }

    }
    public void addRetunedBook(Member member, String bookId, double rating) throws IOException {
        if(pendingReturnedBook.containsKey(member)){

            pendingReturnedBook.get(member).add(new Pair<>(bookId, rating));
            String filePath = "src\\main\\java\\Main\\librarianPendingReturnBooks.txt";
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            boolean found = false;
            for (int i = 0; i < lines.size(); i++){
                if(lines.get(i).contains(member.getUserId()+"|")){
                    found = true;
                    String[] parts = lines.get(i).split("\\|");
                    String[] books = parts[1].split(",");

                    List<String> bookList = new ArrayList<>(Arrays.asList(books));
                    String updatedBooks = String.join(",", bookList);
                    if (!bookId.isEmpty()) {
                        updatedBooks += (updatedBooks.isEmpty() ? "" : ",") + bookId;
                    }
                    parts[1] = updatedBooks;
                    lines.set(i, String.join("|", parts));
                    break;
                }
            }
            Files.write(Paths.get(filePath), lines);
        }
        else{
            List<Pair<String, Double>> list = new ArrayList<>();
            list.add(new Pair<>(bookId, rating));
            pendingReturnedBook.put(member, list);
            BufferedWriter pendingInformation = new BufferedWriter(new FileWriter("src\\main\\java\\Main\\librarianPendingReturnBooks.txt", true));
            pendingInformation.write(member.getUserId() + "|" + "dummybook," + bookId + "\n");
            pendingInformation.close();
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
                if(bookId.equals("dummybook")) continue;
                Book book = serve.findBook(bookId);
                if(book != null){
                    System.out.print("Name: ");
                    System.out.print(book.getName());
                    System.out.print(", Id: ");
                    System.out.println(book.getBookId());
                }
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
                if(bookId.equals("dummybook")) continue;
                Book book = serve.findBook(bookId.first);
                System.out.print("Name: ");
                System.out.print(book.getName());
                System.out.print(", Id: ");
                System.out.println(book.getBookId());
            }
        }
    }
    public void approveBook(service serve, String memberUserId, String bookId) throws IOException {
        Book book = service.findBook(bookId);
        if(book.isAvailable() > 0){
            Member member = serve.isMemberFound(memberUserId);
            member.addBorrowedBook(serve.findBook(bookId));

            pendingIssuingBook.get(member).remove(bookId);
            // changing member information
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

            //changing book avaibility information

            filePath = "src\\main\\java\\Main\\bookInformation.txt";
            lines = Files.readAllLines(Paths.get(filePath));
            found = false;
            for (int i = 0; i < lines.size(); i++){
                if(lines.get(i).contains("|" +bookId+"|")){
                    found = true;
                    String[] parts = lines.get(i).split("\\|");
                    int availabe_copy = Integer.parseInt(parts[7]);
                    parts[7] = String.valueOf(availabe_copy - 1);
                    lines.set(i, String.join("|", parts));
                    break;
                }
            }
            Files.write(Paths.get(filePath), lines);
            book.setAvailable_copies(book.isAvailable() - 1);

            //changing pending books information

            filePath = "src\\main\\java\\Main\\librarianPendingBooks.txt";
            lines = Files.readAllLines(Paths.get(filePath));
            found = false;
            for (int i = 0; i < lines.size(); i++){
                if(lines.get(i).contains(member.getUserId()+"|")){
                    found = true;
                    String[] parts = lines.get(i).split("\\|");
                    String[] books = parts[1].split(",");

                    List<String> bookList = new ArrayList<>(Arrays.asList(books));
                    bookList.remove(bookId);
                    String updatedBooks = String.join(",", bookList);
                    parts[1] = updatedBooks;
                    if(bookList.size() == 1) lines.remove(i);
                    else lines.set(i, String.join("|", parts));
                    break;
                }
            }
            Files.write(Paths.get(filePath), lines);
        }
        else System.out.println("BOOK IS NOT AVAILABLE");
    }
    public void acceptBook(service serve, String memberUserId, String bookId) throws IOException{
        Member member = serve.isMemberFound(memberUserId);
        member.removeBook(serve.findBook(bookId));
        pendingReturnedBook.get(member).remove(bookId);

        //changing member information

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
        Book book = service.findBook(bookId);
        book.setAvailable_copies(book.isAvailable() + 1);
        // changing book avaibility

        filePath = "src\\main\\java\\Main\\bookInformation.txt";
        lines = Files.readAllLines(Paths.get(filePath));
        found = false;
        for (int i = 0; i < lines.size(); i++){
            if(lines.get(i).contains("|" +bookId+"|")){
                found = true;
                String[] parts = lines.get(i).split("\\|");
                int availabe_copy = Integer.parseInt(parts[7]);
                parts[7] = String.valueOf(availabe_copy + 1);
                lines.set(i, String.join("|", parts));
                break;
            }
        }
        Files.write(Paths.get(filePath), lines);

        //changing return book information

        filePath = "src\\main\\java\\Main\\librarianPendingReturnBooks.txt";
        lines = Files.readAllLines(Paths.get(filePath));
        found = false;
        for (int i = 0; i < lines.size(); i++){
            if(lines.get(i).contains(member.getUserId()+"|")){
                found = true;
                String[] parts = lines.get(i).split("\\|");
                String[] books = parts[1].split(",");

                List<String> bookList = new ArrayList<>(Arrays.asList(books));
                bookList.remove(bookId);
                String updatedBooks = String.join(",", bookList);
                parts[1] = updatedBooks;
                if(bookList.size() == 1) lines.remove(i);
                else lines.set(i, String.join("|", parts));
                break;
            }
        }
        Files.write(Paths.get(filePath), lines);
    }
}
