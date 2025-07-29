package service;

import common.Book;
import common.Author;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuthorService {
    protected static List<Author>allAuthors;
    Author author;
    public AuthorService(List<Author> allAuthors){
        AuthorService.allAuthors = allAuthors;
    }
    public AuthorService(Author author){
        this.author = author;
    }

    public static Author getAuthor(String userId) {
        for(var x: allAuthors){
            if(x.getUserId().equals(userId)) return x;
        }
        return null;
    }

    public static List<Author> getAuthors() {
        return allAuthors;
    }

    public static Author isAuthorFound(String userId){
        for(var member: allAuthors){
            if(member.getUserId().equals(userId)) return member;
        }
        return null;
    }

    public void RemoveBook(Book book) throws IOException {
        author.removeBook(book);
        BookService.removeBookFromBookService(book);
        // changing author information
        String filePath = "src\\main\\java\\service\\authorInformation.txt";
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
        filePath = "src\\main\\java\\service\\bookInformation.txt";
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

    public String generateBookId() {
        return String.valueOf(20000 + (int)(Math.random() * 10000));
    }

    public String genetateAuthorId(){
        Author lastAuthor = allAuthors.get(allAuthors.size() - 1);
        int authorId = Integer.parseInt(lastAuthor.getUserId());
        String userId = String.format("%05d", authorId + 1);
        return userId;
    }
    public void addAuthor(Author author){
        allAuthors.add(author);
        try {
            BufferedWriter authorinformation = new BufferedWriter(new FileWriter("src\\main\\java\\Main\\authorInformation.txt", true));
            authorinformation.write("\n" + author.getUserId() + "|" + author.getUserId() + "|" + author.getPassword() + "|" + author.getName() + "|" + "dummybook");
            authorinformation.close();
            System.out.println("Author account created ...");
        } catch (IOException e) {
            System.out.println("Error creating author account " + e.getMessage());
        }
    }


}