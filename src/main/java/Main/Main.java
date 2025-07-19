package Main;

import book.Book;
import service.service;
import user.Author;
import user.Librarian;
import user.Member;

//import java.io.*;
//import java.security.Provider;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        // reading from file;
        BufferedReader bookInformation = new BufferedReader(new FileReader("D:\\CSE\\1-2 project\\testing\\src\\main\\java\\Main\\bookInformation.txt"));
        BufferedReader authorInformation = new BufferedReader(new FileReader("D:\\CSE\\1-2 project\\testing\\src\\main\\java\\Main\\authorInformation.txt"));
        BufferedReader memberInformation = new BufferedReader(new FileReader("D:\\CSE\\1-2 project\\testing\\src\\main\\java\\Main\\memberInformation.txt"));
        BufferedReader librarianInformation = new BufferedReader(new FileReader("D:\\CSE\\1-2 project\\testing\\src\\main\\java\\Main\\librarianInformation.txt"));

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

        bookInformation.close();
        authorInformation.close();
        memberInformation.close();
        librarianInformation.close();
        // loading all information
        List<Book>allBooks = new ArrayList<>();
        List<Member>allMembers = new ArrayList<>();
        List<Author>allAuthors = new ArrayList<>();

        // loading allbooks
        for(var bookInfo: bookList){
            String [] values = bookInfo.split("\\|");
            String [] genre__ = values[3].split(",");
            String [] ratings__ = values[4].split(",");
            List<String> genre = new ArrayList<>();
            List<Double> ratings = new ArrayList<>();
            for(var g: genre__){
                genre.add(g);
            }
            for(var r: ratings__){
                double d = Double.parseDouble(r.trim());
                ratings.add(d);
            }
            Book book = new Book(values[0], values[1], values[2], genre, ratings);
            allBooks.add(book);
        }

        //loading allmembers
        for (var authorInfo: authorList){
            String [] values = authorInfo.split("\\|");
            Author author = new Author(values[0], values[1], values[2], values[3]);
            allAuthors.add(author);
        }

        //loading members
        for (var memberInfo: memberList){
            String [] values = memberInfo.split("\\|");
            Member member = new Member(values[0], values[1], values[2], values[3]);
            allMembers.add(member);
        }

        //loading librarian;
        String [] values = librarian.split("\\|");
        Librarian current_librarian = new Librarian(values[0], values[1], values[2], values[3]);
        service serve = new service(current_librarian, allAuthors, allMembers, allBooks);

        Scanner sc = new Scanner(System.in);
        int choice;
        System.out.println("1. LOGIN AS A MEMBER");
        System.out.println("2. LOGIN AS A AUTHOR");
        System.out.println("3. LOGIN AS A LIBRARIAN");
        System.out.println("4. Create Account: ");
        System.out.print("Enter your Choice: ");
        choice = sc.nextInt();
        sc.nextLine();
        switch (choice){
            case 1:
            {
                String userId;
                String password;
                System.out.print("USER ID: ");
                userId = sc.nextLine();
                Member member = serve.isMemberFound(userId);
                if(member != null){
                    System.out.print("PASSWORD: ");
                    password = sc.nextLine();
                    if(member.getPassword().equals(password)){
                        System.out.println("found it!!!");
                        System.out.println("Enter 1 for viewing all books");
                        int member_choice = sc.nextInt();
                        sc.nextLine();
                        switch (member_choice){
                            case 1 : {
                                serve.showAllBooks();
                                break;
                            }
                        }
                    }
                    else{
                        System.out.println("Wrong password!! Try again");
                    }
                }
                else{
                    System.out.println("UserId not found! Create new Account");
                }
                break;
            }
            case 2:{
                break;
            }
            case 3:{
                break;
            }
            case 4:{
                System.out.println("Enter Account type: 1. Member 2.Author");
                int account_type = sc.nextInt();
                sc.nextLine();
                switch (account_type){
                    case 1:{
                        System.out.print("Enter your name: ");
                        String name = sc.nextLine();
                        System.out.print("Enter your email: ");
                        String email = sc.nextLine();
                        System.out.print("Enter your password: ");
                        String password = sc.nextLine();
                        String userId = serve.generateMemberUserId();
                        System.out.println("Your User Id(please remember it): " + userId);
                        Member member = new Member(email, userId, password, name);
                        serve.addMember(member);
                        break;
                    }
                    case 2:{
                        break;
                    }
                }
            }
        }
    }
}

