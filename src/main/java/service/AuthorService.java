package service;

import book.Book;
import user.Author;
import user.Librarian;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuthorService {

    Author author;

    public AuthorService(Author author){
        this.author = author;
    }

    public void requestPublishBook(String name,String publishedDate,List<String>genre,int totalCopies,Librarian librarian) throws IOException{
        String bookId = generateBookId();
        // with default rating currently 0.0
        List<Double> defaultRatings=new ArrayList<>();
        Book book=new Book(name, bookId, publishedDate, author.getName(), genre, defaultRatings, totalCopies, totalCopies);

        // eequesting librarian to approve the publishing
        librarian.addPendingPublishRequest(author, book);

        System.out.println("book publishing request sent to librarian :");
        System.out.println("Book ID: " + bookId);
        System.out.println("wait for librarian approval.");
    }

    public void RemoveBook(Book book) throws IOException {
        author.removeBook(book);

        // changing author information
        String filePath = "src\\main\\java\\Main\\authorInformation.txt";
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        boolean found = false;
        for (int i = 0; i < lines.size(); i++){
            if(lines.get(i).contains("|" + author.getUserId()+"|")){

                found = true;
                String[] parts = lines.get(i).split("\\|");
                String[] books = parts[4].split(",");

                List<String> bookList = new ArrayList<>(Arrays.asList(books));
                bookList.remove(book.getBookId());
//                System.out.println(lines.get(i));
                String updatedBooks = String.join(",", bookList);
                parts[4] = updatedBooks;
                lines.set(i, String.join("|", parts));
//                System.out.println(lines.get(i));
                break;
            }
        }
        Files.write(Paths.get(filePath), lines);
        //changing book information

        filePath = "src\\main\\java\\Main\\bookInformation.txt";
        lines = Files.readAllLines(Paths.get(filePath));
        found = false;
        for (int i = 0; i < lines.size(); i++){
            if(lines.get(i).contains("|" + book.getBookId() + "|")){
                found = true;
                lines.remove(i);
                break;
            }
        }
        Files.write(Paths.get(filePath), lines);

    }

    public void showPublishedBooks(){
        author.showPublishedBooks();
    }

    private String generateBookId() {
        return String.valueOf(20000 + (int)(Math.random() * 10000));
    }
}