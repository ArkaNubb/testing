package service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import common.Book;
import common.Author;
import common.Librarian;
import common.Member;
import common.SocketWrapper;

public class server {

    public static BookService bookService;
    public static MemberService memberService;
    public static AuthorService authorService;
    public static LibrarianService librarianService;
    public static Librarian currentLibrarian;
    private ServerSocket serverSocket;

    public static Map<String, SocketWrapper> clientMap = new ConcurrentHashMap<>();

    server() {
        // --- IMPORTANT: DO NOT re-initialize the static map here ---
        // clientMap = new HashMap<>(); // REMOVE THIS LINE
        try {
            serverSocket = new ServerSocket(44444);
            System.out.println("Server started. Waiting for clients...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                serve(clientSocket);
            }
        } catch (Exception e) {
            System.out.println("Server error:" + e);
            e.printStackTrace();
        }
    }

    public void serve(Socket clientSocket) throws IOException, ClassNotFoundException {
        SocketWrapper socketWrapper = new SocketWrapper(clientSocket);
//        String clientName = (String) socketWrapper.read();
//        clientMap.put(clientName, socketWrapper);
        new ReadThreadServer(socketWrapper);
//        new WriteThreadServer(socketWrapper, null, null);
    }

    public static void main(String args[]) throws IOException {
        // Static services to be accessible from all UI controllers

        // reading from file;
        BufferedReader bookInformation = new BufferedReader(new FileReader("src/main/java/service/bookInformation.txt"));
        BufferedReader authorInformation = new BufferedReader(new FileReader("src/main/java/service/authorInformation.txt"));
        BufferedReader memberInformation = new BufferedReader(new FileReader("src/main/java/service/memberInformation.txt"));
        BufferedReader librarianInformation = new BufferedReader(new FileReader("src/main/java/service/librarianInformation.txt"));

        // reading booklist;
        List<String> bookList = new ArrayList<>();
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

        bookInformation.close();
        authorInformation.close();
        memberInformation.close();
        librarianInformation.close();

        List<Book>allBooks = new ArrayList<>();
        List<Member>allMembers = new ArrayList<>();
        List<Author>allAuthors = new ArrayList<>();

        // loading allbooks
        for(var bookInfo: bookList){
            if (bookInfo.trim().isEmpty()) continue;
            String [] values = bookInfo.split("\\|");
            if (values.length < 8) continue;
            String [] genre__ = values[4].split(",");
            String [] ratings__ = values[5].split(",");
            List<String> genre = new ArrayList<>(Arrays.asList(genre__));
            List<Double> ratings = new ArrayList<>();
            if (!values[5].equalsIgnoreCase("dummyrating") && !values[5].trim().isEmpty()) {
                for(var r: ratings__){
                    if(!r.trim().isEmpty()) ratings.add(Double.parseDouble(r.trim()));
                }
            }
            int total_copies = Integer.parseInt(values[6].trim());
            int available_copies = Integer.parseInt(values[7].trim());
            Book book = new Book(values[0], values[1], values[2], values[3], genre, ratings, total_copies, available_copies);
            allBooks.add(book);
        }
        bookService = new BookService(allBooks);
        //loading authors
        for (var authorInfo: authorList){
            if (authorInfo.trim().isEmpty()) continue;
            String [] values = authorInfo.split("\\|");
            if (values.length < 5) continue;
            String [] bookIds = values[4].split(",");
            List<Book>authorPublishedBooks = new ArrayList<>();
            for (var x: bookIds){
                if(!x.equalsIgnoreCase("dummybook") && !x.trim().isEmpty()){
                    authorPublishedBooks.add(BookService.findBook(x.trim()));
                }
            }
            Author author = new Author(values[0], values[1], values[2], values[3], authorPublishedBooks);
            allAuthors.add(author);
        }
        authorService = new AuthorService(allAuthors);

        //loading members
        for (var memberInfo: memberList){
            if (memberInfo.trim().isEmpty()) continue;
            String [] valuess = memberInfo.split("\\|");
            if (valuess.length < 5) continue;
            String [] bookIds = valuess[4].split(",");

            List<Book>booklist = new ArrayList<>();
            for(var x: bookIds){
                if(!x.equalsIgnoreCase("dummybook") && !x.trim().isEmpty()) {
                    booklist.add(BookService.findBook(x.trim()));
                }
            }
            Member member = new Member(valuess[0], valuess[1], valuess[2], valuess[3], booklist);
            allMembers.add(member);
        }
        memberService = new MemberService(allMembers);

        //loading librarian;
        String [] values = librarian.split("\\|");
        currentLibrarian = new Librarian(values[0], values[1], values[2], values[3]);
        librarianService = new LibrarianService(currentLibrarian);
        new server();
    }
}
