import book.Book;
import service.service;
import user.Author;
import user.Librarian;
import user.Member;

import java.io.*;
import java.security.Provider;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        // reading from file;
        BufferedReader bookInformation = new BufferedReader(new FileReader("bookInformation.txt"));
        BufferedReader authorInformation = new BufferedReader(new FileReader("authorInformation.txt"));
        BufferedReader memberInformation = new BufferedReader(new FileReader("memberInformation.txt"));
        BufferedReader librarianInformation = new BufferedReader(new FileReader("librarianInformation.txt"));

        // reading booklist;
        List<String>bookList = new ArrayList<>();
        String str;
        while((str = bookInformation.readLine()) != null){
            bookList.add(str);
        }

        // reading authorlist;
        List<String>authorList = new ArrayList<>();
        while((str = authorInformation.readLine()) != null){
            authorList.add(str);
        }

        // reading memberlist;
        List<String>memberList = new ArrayList<>();
        while((str = memberInformation.readLine()) != null){
            memberList.add(str);
        }

        // reading libraring;
        String librarian = librarianInformation.readLine();

        // loading all information
        List<Book>allBooks = new ArrayList<>();
        List<Member>allMembers = new ArrayList<>();
        List<Author>allAuthors = new ArrayList<>();

        // loading allbooks
        for(var bookInfo: bookList){
            String [] values = bookInfo.split("|");
            String [] genre__ = values[3].split(",");
            String [] ratings__ = values[4].split(",");
            List<String> genre = new ArrayList<>();
            List<Double> ratings = new ArrayList<>();
            for(var g: genre__){
                genre.add(g);
            }
            for(var r: ratings__){
                double d = Double.parseDouble(r);
                ratings.add(d);
            }
            Book book = new Book(values[0], values[1], values[2], genre, ratings);
            allBooks.add(book);
        }

        //loading allmembers
        for (var authorInfo: authorList){
            String [] values = authorInfo.split("|");
            Author author = new Author(values[0], values[1], values[2], values[3]);
            allAuthors.add(author);
        }

        //loading members
        for (var memberInfo: memberList){
            String [] values = memberInfo.split("|");
            Member member = new Member(values[0], values[1], values[2], values[3]);
            allMembers.add(member);
        }

        //loading librarian;
        String [] values = librarian.split("|");
        Librarian current_librarian = new Librarian(values[0], values[1], values[2], values[3]);
        service serve = new service(current_librarian, allAuthors, allMembers, allBooks);
    }
}

